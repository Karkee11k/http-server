# HTTP Server

An HTTP/1.1 server built from scratch in Java using Java NIO. The project 
explores non-blocking I/O, request parsing, routing, and connection management.

## Features

- HTTP/1.1 request parsing
- Event-driven architecture using `Selector`
- Non-blocking I/O with Java NIO
- Routing for HTTP methods and paths
- Request and response abstraction
- Connection state management
- Units test for core components

## Technologies

- Java
- Java NIO

## Project Structure

```text
src/
 ├── server/
 ├── http/
 ├── router/
 ├── handler/
 └── parser/
```

## Running

```bash
git clone https://github.com/Karkee11k/http-server.git
cd http-server

# Build and run the project using your preferred build tool or IDE.
```

## Example

```http
GET /hello HTTP/1.1
Host: localhost:8080
```

```http
HTTP/1.1 200 OK
Content-Type: text/plain

Hello, World!
```