
# Performance Testing Guide

This guide explains how to test the performance of CSV parsing and unparsing with Apache Daffodil.

## Overview

The application now includes performance measurements for:
- **CSV to XML Parsing** - Converting CSV data to XML format
- **XML to CSV Unparsing** - Converting XML data back to CSV format

Performance metrics include:
- Execution time (milliseconds and seconds)
- Memory usage (MB)
- Throughput (KB/s)
- Input/output file sizes

## Quick Start

### 1. Test with Small CSV (Baseline)

Run the default test with the small `employees.csv` file (~10 lines):

```bash
mvn clean compile exec:java
```

This establishes a baseline performance profile.

### 2. Generate Large CSV File

Generate a large CSV file for performance testing (default: 100,000 lines):

```bash
mvn exec:java -Dexec.args="--generate 100000"
```

You can specify different line counts:
```bash
mvn exec:java -Dexec.args="--generate 500000"    # 500k lines
mvn exec:java -Dexec.args="--generate 1000000"   # 1 million lines
```

The generated file will be saved to `src/main/resources/employees_large.csv`.

### 3. Test with Large CSV

Run performance tests with the large CSV file:

```bash
mvn exec:java -Dexec.args="--large"
```

## Understanding the Metrics

### Performance Report Example

```
============================================================
PERFORMANCE METRICS: CSV to XML Parsing
============================================================
Input size: 5242880 bytes
Duration: 1234 ms (1.234 seconds)
Memory used: 45 MB
Throughput: 4145.23 KB/s
============================================================
```

**Metrics Explained:**
- **Input size**: Size of the input file in bytes
- **Duration**: Total time taken for the operation
- **Memory used**: Additional memory consumed during the operation
- **Throughput**: Data processing rate (kilobytes per second)

## Test Scenarios

### Baseline Test (Small File)
- **Purpose**: Establish baseline performance with minimal overhead
- **Dataset**: ~10 rows, ~500 bytes
- **Expected**: Very fast (<100ms), low memory usage

### Large File Test (100k+ lines)
- **Purpose**: Test scalability and identify bottlenecks
- **Dataset**: 100,000+ rows, several MB
- **Expected**: Higher duration, proportional memory usage

### Stress Test (1M+ lines)
- **Purpose**: Test extreme conditions
- **Dataset**: 1,000,000+ rows, tens of MB
- **Expected**: Longest duration, may require JVM tuning

## Optimizing Performance

### Increase JVM Memory

For very large files, you may need to increase heap size:

```bash
export MAVEN_OPTS="-Xmx4g -Xms1g"
mvn exec:java -Dexec.args="--large"
```

### Run Multiple Tests

To get consistent results, run multiple iterations:

```bash
for i in {1..5}; do
  echo "Run $i"
  mvn exec:java -Dexec.args="--large"
done
```

## Output Files

All output files are saved to the `output/` directory:

- `employees.xml` - Parsed XML output
- `employees_unparsed_from_xml.csv` - Unparsed CSV (round-trip test)

## Troubleshooting

### OutOfMemoryError

If you encounter memory errors with large files:

1. Increase heap size: `-Xmx8g`
2. Reduce file size: `--generate 50000`
3. Enable GC logging: `-verbose:gc`

### Slow Performance

Factors affecting performance:
- Disk I/O speed
- Available RAM
- CPU speed
- JVM version (use Java 11+)
- Operating system overhead

## Comparing Results

Track your results to compare different configurations:

| File Size | Lines | Parse Time | Unparse Time | Memory |
|-----------|-------|------------|--------------|--------|
| Small     | 10    | ~50ms      | ~30ms        | ~5MB   |
| Medium    | 10k   | ~500ms     | ~300ms       | ~50MB  |
| Large     | 100k  | ~5s        | ~3s          | ~500MB |
| Huge      | 1M    | ~50s       | ~30s         | ~2GB   |

*Note: Actual results vary based on hardware and configuration*
