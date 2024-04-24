#!/bin/bash

# Find all Java files recursively in the current directory
java_files=$(find . -name "*.java")

# Initialize total lines count
total_lines=0

# Iterate over each Java file
for file in $java_files; do
    # Count lines in each file and add to total_lines
    lines=$(wc -l < "$file")
    total_lines=$((total_lines + lines))
done

# Print the total number of lines
echo "Total number of lines in Java files: $total_lines"
