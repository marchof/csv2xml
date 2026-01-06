# CSV to XML Converter

Simple converter to convert an CSV file into a validated XML File. The CSV has a specific format which allows to describe arbitrary nested XML files.

While this is a general purpose tool it was created to support the creation of OECD XML tax reports from Excel files.

## Prerequisite

1. Install Java 25, for example [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=25)
2. Download [`CSV2XML.java`](src/main/java/CSV2XML.java)

## Usage

The tool requires 3 parameters:

    java CSV2XML.java <csvfile> <xmlschema> <outputxmlfile>

## CSV File Format

The CSV input file must have exactly 6 columns with the toe following content:

| Column | Description |
| ------ | ----------- |
| 1      | Unique identifier of the node |
| 2      | Identifier of the parent of this node |
| 3      | XML namespace of this node  |
| 4      | Node type `Element` or `Attribute` |
| 5      | Name of this node |
| 6      | Text content the element or value of an attribute |
