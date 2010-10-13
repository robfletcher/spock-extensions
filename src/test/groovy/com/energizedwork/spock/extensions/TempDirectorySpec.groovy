package com.energizedwork.spock.extensions

import spock.lang.*

class TempDirectorySpec extends Specification {

	@TempDirectory File tempDir
	@Shared @TempDirectory File sharedTempDir

	def cleanup() {
		assert !tempDir.isDirectory(), "tempDir should have been deleted before cleanup"
	}

	def cleanupSpec() {
		assert !sharedTempDir.isDirectory(), "sharedTempDir should have been deleted before cleanupSpec"
	}

	def "temp directories are created before feature method"() {
		expect:
		tempDir != null
		tempDir?.isDirectory()

		and:
		sharedTempDir != null
		sharedTempDir?.isDirectory()
	}

	def "files can be added to temp directories"() {
		expect:
		new File(tempDir, "foo").createNewFile()

		and:
		new File(sharedTempDir, "bar").createNewFile()
	}

	def "per feature temp directory is cleaned after each feature method"() {
		expect:
		tempDir.list() == [] as String[]
	}

	def "per spec temp directory is not cleaned after each feature method"() {
		expect:
		sharedTempDir.list() == ["bar"]
	}

}
