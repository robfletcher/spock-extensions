package com.energizedwork.spock.extensions

import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.extension.*

class TempDirectoryExtension extends AbstractAnnotationDrivenExtension<TempDirectory> {

	private static final File tempDir = new File(System.properties."java.io.tmpdir")

	@Override
	void visitFieldAnnotation(TempDirectory annotation, FieldInfo field) {
		def tempDirectory = new File(tempDir, generateFilename(field.name))

		field.parent.getTopSpec().with {
			def interceptor = new TempDirectoryInterceptor(field, tempDirectory)
			if (field.isShared()) {
				setupSpecMethod.addInterceptor interceptor
				cleanupSpecMethod.addInterceptor interceptor
			} else {
				setupMethod.addInterceptor interceptor
				cleanupMethod.addInterceptor interceptor
			}
		}
	}

	private String generateFilename(String baseName) {
		"$baseName-${Long.toHexString(System.currentTimeMillis())}"
	}

}

class TempDirectoryInterceptor extends AbstractMethodInterceptor {

	private final FieldInfo field
	private final File directory

	TempDirectoryInterceptor(FieldInfo field, File directory) {
		this.field = field
		this.directory = directory
	}

	@Override
	void interceptSetupSpecMethod(IMethodInvocation invocation) {
		if (field.shared) setupDirectory(invocation.target)
		invocation.proceed()
	}

	@Override
	void interceptSetupMethod(IMethodInvocation invocation) {
		if (!field.shared) setupDirectory(invocation.target)
		invocation.proceed()
	}

	@Override
	void interceptCleanupMethod(IMethodInvocation invocation) {
		if (!field.shared) destroyDirectory()
		invocation.proceed()
	}

	@Override
	void interceptCleanupSpecMethod(IMethodInvocation invocation) {
		if (field.shared) destroyDirectory()
		invocation.proceed()
	}

	private void setupDirectory(target) {
		directory.mkdirs()
		target[field.name] = directory
	}

	private void destroyDirectory() {
		directory.deleteDir()
	}

}
