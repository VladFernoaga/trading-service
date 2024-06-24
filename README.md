# Trading Service

## Overview

The Trading Service is a high-performance RESTful service designed to handle the rigorous demands of high-frequency trading systems. This service acts as a component in the trading infrastructure, managing and analyzing financial data in near real-time.

## Endpoints

The service exposes three HTTP-based API endpoints, communicating via JSON:

### 1. Add Trading Data Point

**Endpoint**: `POST /api/add`

**Purpose**: To capture and store trading data points identified by unique symbols.

**Request Body**:
```json
{
    "symbol": "AAPL",
    "value": 150.0,
    "timestamp": 1622558473000
}
```
**Response:**
Status: 200 OK
Body: Data added successfully

### 2. Add Batch Trading Data Points

**Endpoint:** `POST /api/add_batch`

**Purpose:** Allows the bulk addition of consecutive trading data points for a specific symbol.

**Request Body:**
```json
{
    "symbol": "AAPL",
    "values": [150.0, 152.0, 153.5]
}
```

**Response:**
- Status: 200 OK
- Body: `Batch data added successfully`

### 3. Get Trading Data Statistics

**Endpoint:** `GET /api/stats`

**Purpose:** To provide rapid statistical analyses of recent trading data for specified symbols.

**Query Parameters:**

- **symbol (string)**: The financial instrument’s identifier (e.g., “AAPL”).
- **k (integer)**: An integer from 1 to 7, specifying the number of last 10^k data points to analyze.

**Response:**

- Status: 200 OK
- Body:

```json
{
    "min": 150.0,
    "max": 153.5,
    "last": 153.5,
    "avg": 151.83,
    "var": 2.23
}
```

## Building and Running the Service

### Prerequisites

- Java 21
- Docker
- Maven Wrapper (included in the repository)

### Building the Application

**1. Clone the repository:**
```bash
git clone https://github.com/yourusername/trading-service.git
cd trading-service
```
**2. Build the application using the Maven Wrapper:**
```bash
git clone https://github.com/yourusername/trading-service.git
cd trading-service
```
### Running the Application with Docker

**1. Build the Docker image:**

```bash
docker build -t trading-service:latest .
```

**2. Run the Docker container:**

```bash 
docker run -d -p 8080:8080 --name trading-service-container trading-service:latest
```

**3. Verify the container is running:**

```bash 
docker logs trading-service-container
```
**4. Access the application:**
      Open a Browser or Postman and navigate to http://localhost:8080.

### Development

To run the service locally for development purposes without Docker:

1. Ensure you have Java 21 installed. 
2. the application using Maven:

The application will start and listen on port 8080.