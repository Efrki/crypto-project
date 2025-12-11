# Technology Stack

## Programming Languages
- **Java 21**: Primary backend language with modern LTS features
- **JavaScript (ES6+)**: Frontend application logic
- **HTML5/CSS3**: User interface markup and styling

## Backend Framework
- **Spring Boot 3.5.7**: Core application framework
  - Spring Web: REST API development
  - Spring Security: Authentication and authorization
  - Spring Data JPA: Database abstraction layer
  - Spring WebSocket: Real-time bidirectional communication
  - Spring AMQP: RabbitMQ integration

## Build System
- **Gradle 8.14.3**: Build automation and dependency management
- **Gradle Wrapper**: Ensures consistent build environment

## Database Systems
- **PostgreSQL**: Primary server-side database for users, chats, contacts
- **H2 Database**: Embedded client-side database for local message storage
  - Console enabled at `/h2-console`
  - In-memory or file-based persistence

## Message Broker
- **RabbitMQ**: Asynchronous message queue for encrypted message fragments
  - Host: localhost:5672
  - Default credentials: guest/guest
  - AMQP protocol for message delivery

## Security Technologies
- **JWT (JSON Web Tokens)**: Stateless authentication
  - Library: `io.jsonwebtoken:jjwt-api:0.12.5`
  - Token expiration: 24 hours (86400000ms)
- **BCrypt**: Password hashing algorithm (via Spring Security)
- **Spring Security**: Request filtering, CORS, authentication

## Development Tools
- **Spring Boot DevTools**: Hot reload during development
- **Lombok**: Boilerplate code reduction (@Data, @Builder, @Slf4j)
  - Annotation processing for getters/setters/constructors

## Testing Framework
- **JUnit Jupiter 5.10.0**: Unit testing framework
- **Spring Boot Test**: Integration testing support
- **Spring Security Test**: Security testing utilities
- **Spring Rabbit Test**: RabbitMQ testing tools

## Frontend Technologies
- **Vanilla JavaScript**: No framework dependencies
- **WebSocket API**: Browser native WebSocket client
- **Fetch API**: HTTP requests to REST endpoints
- **LocalStorage**: Client-side session management

## Key Dependencies

### Core Spring Boot Starters
```gradle
spring-boot-starter-web          # REST API
spring-boot-starter-security     # Authentication
spring-boot-starter-data-jpa     # Database ORM
spring-boot-starter-websocket    # Real-time messaging
spring-boot-starter-amqp         # RabbitMQ
```

### Database Drivers
```gradle
org.postgresql:postgresql        # PostgreSQL JDBC driver
com.h2database:h2               # H2 embedded database
```

### JWT Libraries
```gradle
io.jsonwebtoken:jjwt-api:0.12.5      # JWT API
io.jsonwebtoken:jjwt-impl:0.12.5     # JWT implementation
io.jsonwebtoken:jjwt-jackson:0.12.5  # JSON serialization
```

## Configuration Files

### application.yml
- PostgreSQL connection: `jdbc:postgresql://localhost:5432/crypto_chat`
- JPA settings: `ddl-auto: update`, SQL logging enabled
- H2 console: enabled at `/h2-console`
- RabbitMQ connection parameters

### application.properties
- JWT secret key configuration
- JWT expiration time
- Spring Security default credentials
- JPA schema strategy: `create-drop` (recreates on restart)

## Development Commands

### Build & Run
```bash
./gradlew build          # Compile and build project
./gradlew bootRun        # Run Spring Boot application
./gradlew test           # Execute test suite
./gradlew clean          # Clean build artifacts
```

### Database Setup
```bash
# PostgreSQL must be running on localhost:5432
# Database: crypto_chat
# User: postgres / Password: postgres
```

### RabbitMQ Setup
```bash
# RabbitMQ must be running on localhost:5672
# Default guest/guest credentials
# Management UI: http://localhost:15672
```

## Runtime Requirements
- **Java Runtime**: JDK 21 or higher
- **PostgreSQL**: Version 12+ recommended
- **RabbitMQ**: Version 3.8+ recommended
- **Browser**: Modern browser with WebSocket support

## Port Configuration
- **Application Server**: 8080 (default Spring Boot)
- **PostgreSQL**: 5432
- **RabbitMQ**: 5672 (AMQP), 15672 (Management UI)
- **H2 Console**: /h2-console endpoint

## Cryptographic Implementation
- **Custom Ciphers**: Pure Java implementations (no JCE providers)
- **Block Size**: 128-bit (16 bytes) for RC6 and Twofish
- **Key Sizes**: Configurable per cipher specification
- **Math Utilities**: Custom BigInteger operations for Diffie-Hellman

## Deployment Notes
- **Profile Support**: application-h2.properties for H2-specific config
- **Schema Management**: Hibernate auto-creates tables on startup
- **CORS**: Configured in SecurityConfig for cross-origin requests
- **WebSocket Endpoint**: `/ws` with STOMP protocol support
