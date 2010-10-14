package com.energizedwork.spock.extensions

import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.*

class TempDirectoryExtension extends AbstractAnnotationDrivenExtension<TempDirectory> {

	private static final File tempDir = new File(System.properties."java.io.tmpdir")

	@Override
	void visitFieldAnnotation(TempDirectory annotation, FieldInfo field) {
		def tempDirectory = new File(tempDir, generateFilename(field.name))

		def interceptor
		if (field.isShared()) {
			interceptor = new SharedTempDirectoryInterceptor(field, tempDirectory)
		} else {
			interceptor = new TempDirectoryInterceptor(field, tempDirectory)
		}
		interceptor.install(field.parent.getTopSpec())
	}

	private String generateFilename(String baseName) {
		"$baseName-${Long.toHexString(System.currentTimeMillis())}"
	}

}

abstract class DirectoryManagingInterceptor extends AbstractMethodInterceptor {

	private final FieldInfo field
	private final File directory

	DirectoryManagingInterceptor(FieldInfo field, File directory) {
		this.field = field
		this.directory = directory
	}

	protected void setupDirectory(target) {
		directory.mkdirs()
		target[field.name] = directory
	}

	protected void destroyDirectory() {
		directory.deleteDir()
	}

	abstract void install(SpecInfo spec)

}

@InheritConstructors
class TempDirectoryInterceptor extends DirectoryManagingInterceptor {

	@Override
	void interceptSetupMethod(IMethodInvocation invocation) {
		setupDirectory(invocation.target)
		invocation.proceed()
	}

	@Override
	void interceptCleanupMethod(IMethodInvocation invocation) {
		destroyDirectory()
		invocation.proceed()
	}

	@Override
	void install(SpecInfo spec) {
		spec.setupMethod.addInterceptor this
		spec.cleanupMethod.addInterceptor this
	}

}

@InheritConstructors
class SharedTempDirectoryInterceptor extends DirectoryManagingInterceptor {

	@Override
	void interceptSetupSpecMethod(IMethodInvocation invocation) {
		setupDirectory(invocation.target)
		invocation.proceed()
	}

	@Override
	void interceptCleanupSpecMethod(IMethodInvocation invocation) {
		destroyDirectory()
		invocation.proceed()
	}

	@Override
	void install(SpecInfo spec) {
		spec.setupSpecMethod.addInterceptor this
		spec.cleanupSpecMethod.addInterceptor this
	}

}
