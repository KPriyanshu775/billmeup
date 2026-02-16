# MediVault - Digital Health Record System

MediVault now includes two runnable modes:
- Desktop software UI (Java Swing)
- Console mode (terminal)

## Tech
- Java 17
- Maven

## Run Desktop App (Recommended)
```bash
cd MediVault
mvn compile
mvn exec:java
```

## Run Console Mode
```bash
cd MediVault
mvn exec:java -Dexec.mainClass="com.medivault.app.Main"
```
