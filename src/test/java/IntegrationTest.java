import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IntegrationTest {

	@TempDir
	static Path workdir;

	Path schemafile, csvfile, xmlfile;

	@BeforeEach
	void setup() {
		schemafile = Path.of("src/test/resources/testschema.xsd");
		csvfile = workdir.resolve("input.csv");
		xmlfile = workdir.resolve("output.xml");
	}

	@Test
	void should_transform_valid_input() throws Exception {
		var csvfile = Path.of("src/test/resources/testinput.csv");
		new CSV2XML().run(csvfile, schemafile, xmlfile);

		assertEquals("""
				<?xml version="1.0" encoding="UTF-8" standalone="no"?>
				<message timestamp="2026-01-15T08:30:00" xmlns="http://www.example.org/testschema/">
				    <subject>My Subject</subject>
				    <body>My Content</body>
				</message>
				""", Files.readString(xmlfile));
	}

	@Test
	void should_ignore_empty_lines() throws Exception {
		Files.writeString(csvfile, """
				ID;ParentID;Namespace;Typ;Name;Value
				e1;;http://www.example.org/testschema/;Element;message;
				e2;e1;http://www.example.org/testschema/;Element;subject;My Sybject

				e3;e1;http://www.example.org/testschema/;Element;body;My Content
				\s\s\s
				""");
		new CSV2XML().run(csvfile, schemafile, xmlfile);
	}

}
