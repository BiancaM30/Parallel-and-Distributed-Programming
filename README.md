# Parallel and Distributed Programming

This repository contains solutions to various lab problems focused on parallel and distributed computing. The implementations are written in Java and C++ and use concepts like multithreading, task division, and MPI for distributed computing.

## Lab 1: Image Filtering with Multithreading
- **Problem**: Implement an image filtering algorithm by applying a filter window to pixel values in a matrix using multithreading.
- **Objective**: Efficiently divide tasks across multiple threads for optimal performance using geometric decomposition techniques such as row-based, column-based, and block-based.
- **Tools**: Java and C++ (C++11 or later).
- **Key Concepts**: Parallelism, matrix manipulation, thread-based task distribution.

## Lab 2: Matrix Multiplication using OpenMP
- **Problem**: Implement matrix multiplication in a parallel fashion using OpenMP in C++.
- **Objective**: Speed up matrix multiplication by distributing computation across multiple threads using OpenMP.
- **Key Concepts**: OpenMP directives, task scheduling, performance optimization.
- **Testing**: Run experiments with varying matrix sizes and thread counts to analyze performance improvements.

## Lab 3: Large Number Addition using MPI
- **Problem**: Use MPI to add two large numbers represented as arrays of digits (each with more than 10 digits).
- **Objective**: Break down the addition task across multiple MPI processes and ensure proper handling of carry-over digits.
- **Key Concepts**: MPI_Send, MPI_Recv, MPI_Scatter, and MPI_Gather for communication between processes.
- **Languages**: C++11 with MPI.
- **Testing**: Run tests for different digit lengths and analyze the time taken for the parallel addition.

## Lab 4: Parallel QuickSort
- **Problem**: Implement QuickSort in parallel using the fork-join framework in Java and pthreads in C++.
- **Objective**: Speed up the sorting algorithm by splitting the array into sub-arrays that can be sorted concurrently by multiple threads.
- **Key Concepts**: Fork-join parallelism, synchronization, task division.

## Lab 5: Distributed Data Processing using Hadoop MapReduce
- **Problem**: Use Hadoop MapReduce to process a large dataset in a distributed manner.
- **Objective**: Implement a word count algorithm and analyze large datasets by distributing the computation across a Hadoop cluster.
- **Tools**: Hadoop framework.
- **Key Concepts**: Distributed computing, MapReduce paradigm, data locality.
