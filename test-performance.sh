#!/bin/bash

# Performance Testing Script for Apache Daffodil CSV Parser
# Usage: ./test-performance.sh [baseline|generate|large|stress]

set -e

echo "=========================================="
echo "Apache Daffodil Performance Testing"
echo "=========================================="
echo ""

case "${1:-help}" in
    baseline)
        echo "Running BASELINE test with small CSV file..."
        echo ""
        mvn clean compile exec:java
        ;;
        
    generate)
        LINES=${2:-100000}
        echo "Generating large CSV file with $LINES lines..."
        echo ""
        mvn compile exec:java -Dexec.args="--generate $LINES"
        ;;
        
    large)
        echo "Running LARGE FILE test (100k lines)..."
        echo ""
        if [ ! -f "src/main/resources/employees_large.csv" ]; then
            echo "Large CSV file not found. Generating it first..."
            mvn compile exec:java -Dexec.args="--generate 100000"
            echo ""
            echo "Now running the test..."
            echo ""
        fi
        mvn compile exec:java -Dexec.args="--large"
        ;;
        
    stress)
        echo "Running STRESS test (500k lines)..."
        echo ""
        if [ ! -f "src/main/resources/employees_large.csv" ]; then
            echo "Large CSV file not found. Generating it first..."
            mvn compile exec:java -Dexec.args="--generate 500000"
            echo ""
            echo "Now running the test..."
            echo ""
        fi
        export MAVEN_OPTS="-Xmx4g -Xms1g"
        mvn compile exec:java -Dexec.args="--large"
        ;;
        
    compare)
        echo "Running COMPARISON test (baseline vs large)..."
        echo ""
        echo ">>> BASELINE (small file) <<<"
        mvn clean compile exec:java > /tmp/baseline_results.txt 2>&1
        grep -A 6 "PERFORMANCE METRICS" /tmp/baseline_results.txt
        
        echo ""
        echo ">>> LARGE FILE (100k lines) <<<"
        if [ ! -f "src/main/resources/employees_large.csv" ]; then
            mvn compile exec:java -Dexec.args="--generate 100000"
        fi
        mvn compile exec:java -Dexec.args="--large" > /tmp/large_results.txt 2>&1
        grep -A 6 "PERFORMANCE METRICS" /tmp/large_results.txt
        ;;
        
    clean)
        echo "Cleaning up generated files..."
        rm -f src/main/resources/employees_large.csv
        rm -rf output/
        mvn clean
        echo "Done!"
        ;;
        
    help|*)
        echo "Usage: $0 [command] [options]"
        echo ""
        echo "Commands:"
        echo "  baseline          Run test with small CSV file (baseline)"
        echo "  generate [lines]  Generate large CSV file (default: 100000 lines)"
        echo "  large             Run test with large CSV file (100k lines)"
        echo "  stress            Run stress test with 500k lines"
        echo "  compare           Compare baseline vs large file performance"
        echo "  clean             Clean up generated files"
        echo ""
        echo "Examples:"
        echo "  $0 baseline"
        echo "  $0 generate 250000"
        echo "  $0 large"
        echo "  $0 stress"
        echo "  $0 compare"
        echo ""
        ;;
esac
