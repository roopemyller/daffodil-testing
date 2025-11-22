package com.example.daffodil;

import org.apache.daffodil.japi.Compiler;
import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.UnparseResult;
import org.apache.daffodil.japi.ProcessorFactory;
import org.apache.daffodil.japi.infoset.JsonInfosetOutputter;
import org.apache.daffodil.japi.infoset.JsonInfosetInputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.apache.daffodil.japi.infoset.XMLTextInfosetOutputter;
import org.apache.daffodil.japi.infoset.XMLTextInfosetInputter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.daffodil.japi.infoset.XMLTextEscapeStyle;
import org.apache.daffodil.runtime1.infoset.XMLTextInfoset;

/**
 * CSV to JSON/XML Parser using Apache Daffodil DFDL Processor
 * 
 * This example demonstrates:
 * - Compiling a DFDL schema
 * - Parsing CSV data to JSON (parse)
 * - Parsing CSV data to XML (parse)
 * - Unparsing JSON data back to CSV (unparse)
 */
public class CsvToJsonParser {

    // Performance measurement helper
    private static class PerformanceMetrics {
        String operation;
        long startTime;
        long endTime;
        long memoryBefore;
        long memoryAfter;
        int inputSize;
        
        public PerformanceMetrics(String operation, int inputSize) {
            this.operation = operation;
            this.inputSize = inputSize;
        }
        
        public void start() {
            System.gc(); // Suggest garbage collection before measurement
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            Runtime runtime = Runtime.getRuntime();
            memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            startTime = System.nanoTime();
        }
        
        public void end() {
            endTime = System.nanoTime();
            Runtime runtime = Runtime.getRuntime();
            memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        }
        
        public void printReport() {
            long durationMs = (endTime - startTime) / 1_000_000;
            long memoryUsedMB = (memoryAfter - memoryBefore) / (1024 * 1024);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PERFORMANCE METRICS: " + operation);
            System.out.println("=".repeat(60));
            System.out.println("Input size: " + inputSize + " bytes");
            System.out.println("Duration: " + durationMs + " ms (" + String.format("%.3f", durationMs/1000.0) + " seconds)");
            System.out.println("Memory used: " + memoryUsedMB + " MB");
            System.out.println("Throughput: " + String.format("%.2f", (inputSize / 1024.0) / (durationMs / 1000.0)) + " KB/s");
            System.out.println("=".repeat(60));
        }
    }

    public static void main(String[] args) {
        try {
            // Determine which CSV file to use (small or large)
            String csvFileName = "/employees.csv"; // default small file
            if (args.length > 0 && args[0].equals("--large")) {
                csvFileName = "/employees_large.csv";
                System.out.println("Using LARGE CSV file for performance testing\n");
            } else if (args.length > 0 && args[0].equals("--generate")) {
                int numLines = 100000; // default
                if (args.length > 1) {
                    numLines = Integer.parseInt(args[1]);
                }
                generateLargeCSV(numLines);
                return;
            } else {
                System.out.println("Using SMALL CSV file for baseline testing\n");
            }
            
            // Load the DFDL schema and CSV data from resources
            URL schemaURL = CsvToJsonParser.class.getResource("/csv.dfdl.xsd");
            URL dataURL;
            
            if (csvFileName.equals("/employees_large.csv")) {
                // For large file, load from file system
                File largeFile = new File("src/main/resources/employees_large.csv");
                if (!largeFile.exists()) {
                    System.err.println("Large CSV file not found. Generate it first with: mvn exec:java -Dexec.args='--generate 100000'");
                    System.exit(1);
                }
                dataURL = largeFile.toURI().toURL();
            } else {
                dataURL = CsvToJsonParser.class.getResource(csvFileName);
            }

            if (schemaURL == null || dataURL == null) {
                System.err.println("Error: Could not find schema or data file in resources");
                System.exit(1);
            }
            
            // Get file size for performance metrics
            int fileSize = dataURL.openStream().available();


        /*
            System.out.println("========== CSV to JSON Parser using Apache Daffodil ==========\n");

            // Display the original CSV data
            
            System.out.println("Original CSV Data:");
            System.out.println("------------------");
            displayFile(dataURL);
            
            
            // Parse CSV to JSON
            String jsonOutput = parseCSVtoJSON(schemaURL, dataURL);

            
            System.out.println("\nParsed JSON Output:");
            System.out.println("-------------------");
            System.out.println(jsonOutput);
            

            // write JSON output to a file
            String outputPath = "output/employees.json";
            Files.createDirectories(Paths.get("output"));
            Files.write(Paths.get(outputPath), jsonOutput.getBytes());
            System.out.println("\nJSON output written to: " + new File(outputPath).getAbsolutePath());

            System.out.println("\n========== Parsing Completed Successfully ==========");

            // Unparse JSON back to CSV
            System.out.println("\n\n========== JSON to CSV Unparser using Apache Daffodil ==========\n");
            String csvOutput = unparseJSONtoCSV(schemaURL, jsonOutput);

            
            System.out.println("\nUnparsed CSV Output:");
            System.out.println("--------------------");
            System.out.println(csvOutput);
            

            // Write CSV output to a file
            String csvOutputPath = "output/employees_unparsed.csv";
            Files.write(Paths.get(csvOutputPath), csvOutput.getBytes());
            System.out.println("\nCSV output written to: " + new File(csvOutputPath).getAbsolutePath());

            System.out.println("\n========== Unparsing Completed Successfully ==========");
        */

            // Parse CSV to XML with performance measurement
            System.out.println("\n\n========== CSV to XML Parser using Apache Daffodil ==========\n");
            PerformanceMetrics parseMetrics = new PerformanceMetrics("CSV to XML Parsing", fileSize);
            parseMetrics.start();
            String xmlOutput = parseCSVtoXML(schemaURL, dataURL);
            parseMetrics.end();

            // Write XML output to a file
            String xmlOutputPath = "output/employees.xml";
            Files.createDirectories(Paths.get("output"));
            Files.write(Paths.get(xmlOutputPath), xmlOutput.getBytes());
            System.out.println("\nXML output written to: " + new File(xmlOutputPath).getAbsolutePath());
            System.out.println("Output size: " + (xmlOutput.length() / 1024) + " KB");

            System.out.println("\n========== XML Parsing Completed Successfully ==========");
            parseMetrics.printReport();

            // Unparse XML back to CSV with performance measurement
            System.out.println("\n\n========== XML to CSV Unparser using Apache Daffodil ==========\n");
            PerformanceMetrics unparseMetrics = new PerformanceMetrics("XML to CSV Unparsing", xmlOutput.length());
            unparseMetrics.start();
            String csvOutputFromXML = unparseXMLtoCSV(schemaURL, xmlOutput);  
            unparseMetrics.end();

            // Write CSV output to a file
            String csvOutputFromXMLPath = "output/employees_unparsed_from_xml.csv";
            Files.write(Paths.get(csvOutputFromXMLPath), csvOutputFromXML.getBytes());
            System.out.println("\nCSV output written to: " + new File(csvOutputFromXMLPath).getAbsolutePath());
            System.out.println("Output size: " + (csvOutputFromXML.length() / 1024) + " KB");

            System.out.println("\n========== XML Unparsing Completed Successfully ==========");
            unparseMetrics.printReport();

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parse CSV data to JSON using Daffodil DFDL processor
     */
    private static String parseCSVtoJSON(URL schemaURL, URL dataURL) 
            throws IOException, URISyntaxException {
        
        // Step 1: Compile the DFDL schema
        System.out.println("\nStep 1: Compiling DFDL Schema...");
        Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(schemaURL.toURI());

        // Check for compilation errors
        if (processorFactory.isError()) {
            System.err.println("Schema compilation failed:");
            List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to compile DFDL schema");
        }
        System.out.println("Schema compiled successfully.");

        // Step 2: Create a DataProcessor
        System.out.println("\nStep 2: Creating Data Processor...");
        DataProcessor dataProcessor = processorFactory.onPath("/");
        
        if (dataProcessor.isError()) {
            System.err.println("DataProcessor creation failed:");
            List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to create DataProcessor");
        }
         System.out.println("DataProcessor created successfully.");

        // Step 3: Prepare input stream for CSV data
        System.out.println("\nStep 3: Preparing input data...");
        InputStream inputStream = dataURL.openStream();
        InputSourceDataInputStream dataInputStream = new InputSourceDataInputStream(inputStream);

        // Step 4: Setup JSON outputter
        System.out.println("\nStep 4: Setting up JSON outputter...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonInfosetOutputter jsonOutputter = new JsonInfosetOutputter(outputStream, true);

        // Step 5: Parse the CSV data
        System.out.println("\nStep 5: Parsing CSV data...");
        ParseResult parseResult = dataProcessor.parse(dataInputStream, jsonOutputter);

        // Check for parsing errors
        if (parseResult.isError()) {
            System.err.println("Parsing failed:");
            List<Diagnostic> diagnostics = parseResult.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to parse CSV data");
        }

        // Display warnings if any
        List<Diagnostic> diagnostics = parseResult.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            System.out.println("\nWarnings during parsing:");
            for (Diagnostic diag : diagnostics) {
                System.out.println("  - " + diag.getSomeMessage());
            }
        }

        System.out.println("Parsing completed successfully.");

        // Return the JSON output
        return outputStream.toString();
    }

    /**
     * Unparse JSON data back to CSV using Daffodil DFDL processor
     */
    private static String unparseJSONtoCSV(URL schemaURL, String jsonData) 
            throws IOException, URISyntaxException {
        
        // Step 1: Compile the DFDL schema
        System.out.println("\nStep 1: Compiling DFDL Schema for unparsing...");
        Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(schemaURL.toURI());

        // Check for compilation errors
        if (processorFactory.isError()) {
            System.err.println("Schema compilation failed:");
            List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to compile DFDL schema");
        }
        System.out.println("Schema compiled successfully.");

        // Step 2: Create a DataProcessor
        System.out.println("\nStep 2: Creating Data Processor for unparsing...");
        DataProcessor dataProcessor = processorFactory.onPath("/");
        
        if (dataProcessor.isError()) {
            System.err.println("DataProcessor creation failed:");
            List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to create DataProcessor");
        }
        System.out.println("DataProcessor created successfully.");

        // Step 3: Prepare JSON input
        System.out.println("\nStep 3: Preparing JSON input data...");
        ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonData.getBytes());
        JsonInfosetInputter jsonInputter = new JsonInfosetInputter(jsonInputStream);

        // Step 4: Setup output channel for CSV data
        System.out.println("\nStep 4: Setting up CSV output channel...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WritableByteChannel outputChannel = Channels.newChannel(outputStream);

        // Step 5: Unparse the JSON data to CSV
        System.out.println("\nStep 5: Unparsing JSON data to CSV...");
        UnparseResult unparseResult = dataProcessor.unparse(jsonInputter, outputChannel);

        // Check for unparsing errors
        if (unparseResult.isError()) {
            System.err.println("Unparsing failed:");
            List<Diagnostic> diagnostics = unparseResult.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to unparse JSON data");
        }

        // Display warnings if any
        List<Diagnostic> diagnostics = unparseResult.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            System.out.println("\nWarnings during unparsing:");
            for (Diagnostic diag : diagnostics) {
                System.out.println("  - " + diag.getSomeMessage());
            }
        }

        System.out.println("Unparsing completed successfully.");

        // Return the CSV output
        return outputStream.toString();
    }

    /**
     * Parse CSV data to XML using Daffodil DFDL processor
     */
    private static String parseCSVtoXML(URL schemaURL, URL dataURL) 
            throws IOException, URISyntaxException {
        
        // Step 1: Compile the DFDL schema
        System.out.println("\nStep 1: Compiling DFDL Schema for XML output...");
        Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(schemaURL.toURI());

        // Check for compilation errors
        if (processorFactory.isError()) {
            System.err.println("Schema compilation failed:");
            List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to compile DFDL schema");
        }
        System.out.println("Schema compiled successfully.");

        // Step 2: Create a DataProcessor
        System.out.println("\nStep 2: Creating Data Processor for XML parsing...");
        DataProcessor dataProcessor = processorFactory.onPath("/");
        
        if (dataProcessor.isError()) {
            System.err.println("DataProcessor creation failed:");
            List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to create DataProcessor");
        }
        System.out.println("DataProcessor created successfully.");

        // Step 3: Prepare input stream for CSV data
        System.out.println("\nStep 3: Preparing input data...");
        InputStream inputStream = dataURL.openStream();
        InputSourceDataInputStream dataInputStream = new InputSourceDataInputStream(inputStream);

        // Step 4: Setup XML outputter
        System.out.println("\nStep 4: Setting up XML outputter...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLTextInfosetOutputter xmlOutputter = new XMLTextInfosetOutputter(outputStream, true);

        // Step 5: Parse the CSV data
        System.out.println("\nStep 5: Parsing CSV data to XML...");
        ParseResult parseResult = dataProcessor.parse(dataInputStream, xmlOutputter);

        // Check for parsing errors
        if (parseResult.isError()) {
            System.err.println("Parsing failed:");
            List<Diagnostic> diagnostics = parseResult.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to parse CSV data");
        }

        // Display warnings if any
        List<Diagnostic> diagnostics = parseResult.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            System.out.println("\nWarnings during parsing:");
            for (Diagnostic diag : diagnostics) {
                System.out.println("  - " + diag.getSomeMessage());
            }
        }

        System.out.println("XML parsing completed successfully.");

        // Return the XML output
        return outputStream.toString();
    }

     /**
     * Unparse JSON data back to CSV using Daffodil DFDL processor
     */
    private static String unparseXMLtoCSV(URL schemaURL, String jsonData) 
            throws IOException, URISyntaxException {
        
        // Step 1: Compile the DFDL schema
        System.out.println("\nStep 1: Compiling DFDL Schema for unparsing...");
        Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(schemaURL.toURI());

        // Check for compilation errors
        if (processorFactory.isError()) {
            System.err.println("Schema compilation failed:");
            List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to compile DFDL schema");
        }
        System.out.println("Schema compiled successfully.");

        // Step 2: Create a DataProcessor
        System.out.println("\nStep 2: Creating Data Processor for unparsing...");
        DataProcessor dataProcessor = processorFactory.onPath("/");
        
        if (dataProcessor.isError()) {
            System.err.println("DataProcessor creation failed:");
            List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to create DataProcessor");
        }
        System.out.println("DataProcessor created successfully.");

        // Step 3: Prepare XML input
        System.out.println("\nStep 3: Preparing JSON input data...");
        ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonData.getBytes());
        XMLTextInfosetInputter xmlInputter = new XMLTextInfosetInputter(jsonInputStream);

        // Step 4: Setup output channel for CSV data
        System.out.println("\nStep 4: Setting up CSV output channel...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        WritableByteChannel outputChannel = Channels.newChannel(outputStream);

        // Step 5: Unparse the JSON data to CSV
        System.out.println("\nStep 5: Unparsing JSON data to CSV...");
        UnparseResult unparseResult = dataProcessor.unparse(xmlInputter, outputChannel);

        // Check for unparsing errors
        if (unparseResult.isError()) {
            System.err.println("Unparsing failed:");
            List<Diagnostic> diagnostics = unparseResult.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                System.err.println("  - " + diag.getSomeMessage());
            }
            throw new RuntimeException("Failed to unparse XML data");
        }

        // Display warnings if any
        List<Diagnostic> diagnostics = unparseResult.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            System.out.println("\nWarnings during unparsing:");
            for (Diagnostic diag : diagnostics) {
                System.out.println("  - " + diag.getSomeMessage());
            }
        }

        System.out.println("Unparsing completed successfully.");

        // Return the CSV output
        return outputStream.toString();
    }

    /**
     * Generate a large CSV file for performance testing
     */
    private static void generateLargeCSV(int numLines) throws IOException {
        System.out.println("Generating large CSV file with " + numLines + " lines...");
        
        String outputPath = "src/main/resources/employees_large.csv";
        Files.createDirectories(Paths.get("src/main/resources"));
        
        String[] firstNames = {"Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "George", "Hannah", "Ian", "Jane",
                               "Kevin", "Laura", "Michael", "Nancy", "Oliver", "Patricia", "Quinn", "Rachel", "Steve", "Tina"};
        String[] departments = {"Engineering", "Marketing", "Sales", "HR", "Finance", "Operations", "IT", "Legal"};
        String[] locations = {"New York", "Chicago", "San Francisco", "Boston", "Seattle", "Austin", "Denver", "Miami"};
        String[] yesNo = {"Yes", "No"};
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Write header
            String header = "Name,Age,Department,Salary,Age,Experience,Location,FullTime\n";
            baos.write(header.getBytes());
            
            // Generate data rows
            for (int i = 0; i < numLines; i++) {
                String name = firstNames[i % firstNames.length] + "_" + i;
                int age = 25 + (i % 40);
                String department = departments[i % departments.length];
                int salary = 50000 + (i % 100) * 1000;
                int experience = i % 30;
                String location = locations[i % locations.length];
                String fullTime = yesNo[i % 2];
                
                String row = String.format("%s,%d,%s,%d,%d,%d,%s,%s\n",
                    name, age, department, salary, age, experience, location, fullTime);
                baos.write(row.getBytes());
                
                if ((i + 1) % 10000 == 0) {
                    System.out.println("  Generated " + (i + 1) + " lines...");
                }
            }
            
            Files.write(Paths.get(outputPath), baos.toByteArray());
        }
        
        File outputFile = new File(outputPath);
        System.out.println("\nLarge CSV file generated successfully!");
        System.out.println("File: " + outputFile.getAbsolutePath());
        System.out.println("Size: " + String.format("%.2f", outputFile.length() / (1024.0 * 1024.0)) + " MB");
        System.out.println("Lines: " + numLines);
        System.out.println("\nTo test with this file, run: mvn exec:java -Dexec.args='--large'");
    }

    /**
     * Display the contents of a file
     */
    private static void displayFile(URL fileURL) throws IOException {
        try (InputStream is = fileURL.openStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println();
    }
}
