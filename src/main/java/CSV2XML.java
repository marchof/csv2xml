
/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * Licensed under the MIT license. See LICENSE.md file for details.
 *
 * SPDX-License-Identifier: MIT
 *******************************************************************************/
import static java.util.function.Predicate.not;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CSV2XML {

	void main(String... args) throws Exception {
		run(Path.of(args[0]), Path.of(args[1]), Path.of(args[2]));
	}

	void run(Path csvfile, Path schemafile, Path outputxml) throws Exception {
		var document = buildDocument(csvfile);
		validate(document, schemafile);
		write(document, outputxml);
	}

	Document buildDocument(Path csvfile) throws Exception {
		var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		var nodes = new HashMap<String, Node>();
		var document = builder.newDocument();
		nodes.put("", document);

		parse(csvfile).forEach(row -> {
			var parent = nodes.get(row.parentId);
			if (parent == null) {
				throw new IllegalArgumentException("Unknown parent id: " + row.parentId);
			}
			var node = switch (row.type) {
			case Element -> {
				var e = document.createElementNS(row.namespace(), row.name);
				parent.appendChild(e);
				yield e;
			}
			case Attribute -> {
				var a = document.createAttribute(row.name);
				parent.getAttributes().setNamedItem(a);
				yield a;
			}
			};
			node.setTextContent(row.value);
			if (nodes.put(row.id, node) != null) {
				throw new IllegalArgumentException("Duplicate id: " + row.id);
			}
		});

		return document;
	}

	enum ElementType {
		Element, Attribute
	}

	record XMLDataRow(String id, String parentId, String namespace, ElementType type, String name, String value) {
		XMLDataRow(String csvrow) {
			var cols = csvrow.split(";");
			if (cols.length < 5) {
				throw new IllegalArgumentException("Not enough columns in this line: " + csvrow);
			}
			this(cols[0], cols[1], cols[2], ElementType.valueOf(cols[3]), cols[4], cols.length > 5 ? cols[5] : "");
		}
	}

	Stream<XMLDataRow> parse(Path csvfile) throws Exception {
		return Files.lines(csvfile) //
				.skip(1) // skip header
				.filter(not(String::isBlank)) // skip empty lines
				.map(XMLDataRow::new);
	}

	void validate(Document document, Path schemafile) throws Exception {
		var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		var schemaFile = new StreamSource(schemafile.toFile());
		var schema = factory.newSchema(schemaFile);
		schema.newValidator().validate(new DOMSource(document));
	}

	void write(Document document, Path outputxml) throws Exception {
		var transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(new DOMSource(document), new StreamResult(Files.newBufferedWriter(outputxml)));
	}

}
