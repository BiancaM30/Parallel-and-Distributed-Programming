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

## Lab 4: Polynomial Addition Using Producer-Consumer Pattern
- **Problem**: Add multiple polynomials using multithreading. Polynomials are represented as linked lists of monomials, and a queue is used to manage monomials. One thread reads monomials and adds them to the queue, while worker threads consume them, updating the result polynomial. Synchronization is performed at the node level of the linked list.
- **Objective**: Understand and implement the producer-consumer pattern and synchronization.
- **Language**: Java or C++
- **Performance Analysis**: Compare sequential and parallel implementations for various polynomials of different sizes.

## Lab 5: Enhanced Producer-Consumer with Locking at Node Level
  - **Objective**: Enhance the producer-consumer pattern with fine-grained synchronization.
  - **Problem**: Modify the solution from Lab 4 to use two types of threads: readers and workers. Synchronization between the reader and worker threads is implemented using wait/notify, and node-level locks are used for the linked list. If a node has a zero coefficient, it is removed from the list.
  - **Language**: Java or C++
  - **Performance Analysis**: Compare the performance of the sequential solution and parallel solutions for different thread counts and configurations.
