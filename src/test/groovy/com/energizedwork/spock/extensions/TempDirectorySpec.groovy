package com.energizedwork.spock.extensions

import spock.lang.*

class TempDirectorySpec extends Specification {

    @TempDirectory(clean=true) File tempDir
    @TempDirectory File cleanFalseDir
    @Shared @TempDirectory File sharedTempDir
    @Shared Iterable<File> cleanFalseDirs = []
    @Shared Iterable<File> tempDirs = []

    def cleanup() {
        // We can't check the tempDir got deleted after cleanup in the cleanup method.
        tempDirs << tempDir
        cleanFalseDirs << cleanFalseDir
    }

    def cleanupSpec() {
        cleanFalseDirs.each {
            assert it.exists(), "cleanFalseDir should not have been deleted before cleanup"
        }

        tempDirs.each {
            assert !it.isDirectory(), "tempDir (${it}) should have been deleted"
        }

        // Clean up after ourselves
        cleanFalseDirs.each {
            assert it.deleteDir(), "unable to clean cleanFalseDir"
        }

        // We can't test this from our test, we'd have to use an ExternalSpec Runner to run a different spec
        //assert sharedTempDir.exists(), "sharedTempDir should not have been deleted before cleanupSpec"
    }

    def "temp directories are created before feature method"() {
        expect:
        cleanFalseDir != null
        cleanFalseDir?.isDirectory()

        and:
        sharedTempDir != null
        sharedTempDir?.isDirectory()
    }

    def "files can be added to temp directories"() {
        expect:
        new File(cleanFalseDir, "foo").createNewFile()

        and:
        new File(sharedTempDir, "bar").createNewFile()
    }

    def "per feature temp directory is cleaned after each feature method"() {
        expect:
        cleanFalseDir.list() == [] as String[]
    }

    @Unroll
    def "per feature temp directory is cleaned after each iteration of a data-driven feature"() {
        when:
        new File(cleanFalseDir, filename).createNewFile()

        then:
        cleanFalseDir.list() == [filename]

        where:
        filename << ["foo", "bar", "baz"]
    }

    def "per spec temp directory is not cleaned after each feature method"() {
        expect:
        sharedTempDir.list() == ["bar"]
    }

    def "multiple non-shared temp directories can exist without conflict"() {
        expect:
        cleanFalseDir.canonicalPath != tempDir.canonicalPath
    }
}
