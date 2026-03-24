#!/bin/bash
# Script para ejecutar el backend
# Agregar Maven al PATH
export PATH="/c/Users/victor/Downloads/apache-maven-3.9.6/bin:$PATH"

cd backend
mvn spring-boot:run
