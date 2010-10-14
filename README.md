# Spock Extensions

## @TempDirectory

Used on a `File` property of a spec class this annotation will cause
a temporary directory to be created and injected before each feature
method is run and destroyed afterwards. If the field is `@Shared` the
directory is only destroyed after all feature methods have run. You
can have as many such fields as you like in a single spec, each will
be generated with a unique name.

Temporary directories are created inside `java.io.tmpdir`.

This is useful when testing a class that reads from or writes to a
location on disk.


### Example

	class MySpec extends Specification {

		@TempDirectory File myTempDir

		def diskStore = new DiskStore()

		def "disk store writes bytes to a file"() {
			given:
			diskStore.baseDir = myTempDir
			diskStore.targetFilename = "foo"

			when:
			diskStore << "some text"

			then:
			new File(myTempDir, "foo").text == "some text"
		}

	}