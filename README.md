# CSV to JSON Parser with Apache Daffodil

A simple Java project demonstrating how to parse CSV files and convert them to JSON format using Apache Daffodil's DFDL (Data Format Description Language) processor.

## Project Structure

```
daffodil-testing/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── daffodil/
│       │               └── CsvToJsonParser.java
│       └── resources/
│           ├── csv.dfdl.xsd
│           └── employees.csv
└── README.md
```

## What is Apache Daffodil?

Apache Daffodil is an implementation of the Data Format Description Language (DFDL) specification. It allows you to describe the format of your data (CSV, binary, fixed-width, etc.) using an XML schema, and then use that schema to parse the data into XML or JSON.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean compile
```

## Running the Parser

```bash
mvn exec:java
```

Or after building:

```bash
mvn exec:java -Dexec.mainClass="com.example.daffodil.CsvToJsonParser"
```

## How It Works

1. **DFDL Schema** (`csv.dfdl.xsd`): Defines the structure of CSV data
   - Header row with field names
   - Multiple data records with fields separated by commas
   - Lines separated by newlines

2. **Java Parser** (`CsvToJsonParser.java`):
   - Compiles the DFDL schema
   - Creates a DataProcessor
   - Parses CSV input using the schema
   - Outputs the result as formatted JSON

3. **Sample Data** (`employees.csv`):
   - Contains employee information (Name, Age, Department, Salary)
   - Used as example input for the parser

## Example Output

The parser reads the CSV file:
```csv
Name,Age,Department,Salary
John Doe,30,Engineering,75000
Jane Smith,28,Marketing,65000
```

And converts it to JSON format:
```json
{
  "CSV": {
    "header": {
      "field": ["Name", "Age", "Department", "Salary"]
    },
    "record": [
      {
        "field": ["John Doe", "30", "Engineering", "75000"]
      },
      {
        "field": ["Jane Smith", "28", "Marketing", "65000"]
      }
    ]
  }
}
```

## Key Daffodil API Components

- **Daffodil.compiler()**: Factory for creating a compiler
- **Compiler**: Compiles DFDL schemas into ProcessorFactory
- **ProcessorFactory**: Creates DataProcessor instances
- **DataProcessor**: Performs parsing and unparsing operations
- **JsonInfosetOutputter**: Outputs parsed data as JSON
- **InputSourceDataInputStream**: Wraps input data for parsing

## Additional Resources

- [Apache Daffodil Documentation](https://daffodil.apache.org/)
- [Daffodil Java API JavaDoc](https://daffodil.apache.org/docs/latest/javadoc/)
- [OpenDFDL Examples](https://github.com/OpenDFDL/examples)
- [DFDL Specification](https://ogf.org/ogf/doku.php/standards/dfdl/dfdl)

## Customization

To parse your own CSV files:

1. Modify `employees.csv` with your data
2. Adjust `csv.dfdl.xsd` if your CSV has different delimiters or structure
3. Run the parser to see the JSON output

## License

This project is provided as an example for educational purposes.
