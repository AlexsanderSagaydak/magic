# Wizards Application - Complete API Reference

Comprehensive API reference with detailed method signatures, parameters, return values, and usage examples.

## Quick Reference

### Base URL
```
http://localhost:8080
```

### Authentication
All endpoints except `/`, `/login`, `/users/register`, and `/h2-console/**` require authentication with `ROLE_USER` authority.

---

## HTTP Endpoints

### User Registration Endpoints

#### GET /users/register
Display user registration form.

**Response:**
- Status: 200 OK
- Content-Type: text/html
- Body: HTML registration form with input fields for username, email, password, firstName, lastName, specialization

**Example:**
```bash
curl http://localhost:8080/users/register
```

---

#### POST /users/register
Register a new user account.

**Request Body:**
```json
{
  "username": "john_wizard",
  "email": "john@wizards.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Wizard",
  "specialization": "White Magic"
}
```

**Response on Success:**
- Status: 302 Found (Redirect)
- Location: `/users/success?id=1&name=John`
- Side Effects:
  - User is saved to database with BCrypt-encoded password
  - User is automatically authenticated (logged in)
  - Session is created with `SPRING_SECURITY_CONTEXT`

**Response on Error:**
- Status: 200 OK
- Content-Type: text/html
- Body: Registration form with error message

**Validation Rules:**
- Username must be unique
- Email must be unique and valid format
- Password must be non-empty
- All fields are required

**Example:**
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=john_wizard&email=john@wizards.com&password=SecurePass123!&firstName=John&lastName=Wizard&specialization=White%20Magic"
```

---

#### GET /users/success
Display registration success confirmation page.

**Query Parameters:**
- `id` (optional, Integer): User ID
- `name` (optional, String): User's first name

**Response:**
- Status: 200 OK
- Content-Type: text/html
- Body: Success confirmation page with welcome message

**Requires Authentication:** Yes

**Example:**
```bash
curl http://localhost:8080/users/success?id=1&name=John \
  -H "Cookie: JSESSIONID=<session-id>"
```

---

#### GET /users/{id}
Retrieve and display a user's profile page.

**Path Parameters:**
- `id` (Integer, required): The user's ID

**Response on Success:**
- Status: 200 OK
- Content-Type: text/html
- Body: User profile page with user information

**Response if Not Found:**
- Status: 200 OK
- Content-Type: text/html
- Body: Error page

**Requires Authentication:** Yes

**Example:**
```bash
curl http://localhost:8080/users/1 \
  -H "Cookie: JSESSIONID=<session-id>"
```

---

### Home & Authentication Endpoints

#### GET /
Home page endpoint.

**Response for Authenticated Users:**
- Status: 200 OK
- Content-Type: text/html
- Body: Home page with welcome message and username display

**Response for Unauthenticated Users:**
- Status: 302 Found (Redirect)
- Location: `/login`

**Requires Authentication:** No (but redirects to login if not authenticated)

**Example:**
```bash
curl http://localhost:8080/
```

---

#### GET /login
Display login form.

**Response for Authenticated Users:**
- Status: 302 Found (Redirect)
- Location: `/`

**Response for Unauthenticated Users:**
- Status: 200 OK
- Content-Type: text/html
- Body: Login form with username and password fields

**Requires Authentication:** No

**Example:**
```bash
curl http://localhost:8080/login
```

---

#### POST /login
Authenticate user (handled by Spring Security).

**Request Body (Form-Encoded):**
```
username=<username>&password=<password>&_csrf=<csrf-token>
```

**Response on Success:**
- Status: 302 Found (Redirect)
- Location: `/` (or return_url if specified)
- Side Effects: Session is created with authentication token

**Response on Failure:**
- Status: 302 Found (Redirect)
- Location: `/login?error=true`

**Example:**
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -c cookies.txt \
  -d "username=john_wizard&password=SecurePass123!&_csrf=<csrf-token>"
```

---

#### POST /logout
Logout the current user (handled by Spring Security).

**Response:**
- Status: 302 Found (Redirect)
- Location: `/login`
- Side Effects: Session is invalidated, authentication is removed

**Requires Authentication:** Yes

**Example:**
```bash
curl -X POST http://localhost:8080/logout \
  -H "Cookie: JSESSIONID=<session-id>"
```

---

## Service Layer API

All public methods documented in `JAVADOC.md` can be called directly in Java code.

### UserService Example Usage

```java
// Inject the service
@Autowired
private UserService userService;

// Save a new user
User user = new User("john", "john@example.com", "encodedPassword",
                     "John", "Doe", "White Magic");
User savedUser = userService.saveUser(user);
System.out.println("Saved user ID: " + savedUser.getId());

// Retrieve user by username
Optional<User> user = userService.getUserByUsername("john");
if (user.isPresent()) {
    System.out.println("Email: " + user.get().getEmail());
}

// Check if username exists
boolean exists = userService.usernameExists("john");
if (exists) {
    System.out.println("Username is already taken");
}

// Get all users
List<User> allUsers = userService.getAllUsers();
System.out.println("Total users: " + allUsers.size());

// Update user information
user.setFirstName("Jonathan");
userService.updateUser(user);

// Delete user
userService.deleteUser(1);
```

---

## Repository API

### UserRepository Example Usage

```java
// Inject the repository
@Autowired
private UserRepository userRepository;

// Find by username
Optional<User> user = userRepository.findByUsername("john");

// Find by email
Optional<User> user = userRepository.findByEmail("john@example.com");

// Check existence
boolean usernameExists = userRepository.existsByUsername("john");
boolean emailExists = userRepository.existsByEmail("john@example.com");

// CRUD operations (inherited from JpaRepository)
User savedUser = userRepository.save(user);        // Insert/Update
Optional<User> user = userRepository.findById(1);  // Find by ID
List<User> allUsers = userRepository.findAll();    // Get all
userRepository.deleteById(1);                       // Delete by ID
```

---

## Error Handling

### HTTP Status Codes

| Code | Scenario |
|------|----------|
| 200 | Successful GET request |
| 302 | Redirect (login/logout/register success) |
| 400 | Bad request (validation error) |
| 401 | Unauthorized (not authenticated) |
| 403 | Forbidden (CSRF token missing) |
| 404 | Not found (user doesn't exist) |
| 500 | Server error |

### Exception Handling in Services

**UsernameNotFoundException**
```java
try {
    UserDetails user = customUserDetailsService.loadUserByUsername("nonexistent");
} catch (UsernameNotFoundException e) {
    System.out.println("User not found: " + e.getMessage());
}
```

---

## Security Features

### Password Encoding

All passwords are encoded using **BCrypt** (strength 10) before storage.

```java
// Encoding
@Autowired
private PasswordEncoder passwordEncoder;

String encodedPassword = passwordEncoder.encode("plainTextPassword");

// Verification (done automatically by Spring Security)
boolean matches = passwordEncoder.matches("plainTextPassword", encodedPassword);
```

### CSRF Protection

All POST/PUT/DELETE requests require a valid CSRF token.

**Obtaining CSRF Token:**
```html
<!-- In Thymeleaf templates -->
<input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
```

### Session Management

**Session Configuration:**
- Policy: Always create session (`SessionCreationPolicy.ALWAYS`)
- Persistence: Cookie-based with `JSESSIONID`
- Storage: Server-side (HttpSession)

**Authentication Persistence:**
```java
// After authentication, store in session
session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

// Spring Security retrieves authentication from session on each request
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

---

## Internationalization (i18n)

### Supported Languages

- **English** (default): `?lang=en`
- **Russian**: `?lang=ru`

### Changing Language

```
GET /users/register?lang=en    // English
GET /users/register?lang=ru    // Russian
```

Language preference is stored in HTTP cookies and persists across requests.

### Message Keys

All UI messages are defined in properties files:
- `src/main/resources/messages.properties` (English)
- `src/main/resources/messages_ru.properties` (Russian)

**Example Usage in Thymeleaf:**
```html
<h1 th:text="#{register.title}">Register</h1>
<label th:text="#{register.form.username}">Username</label>
```

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    specialization VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);
```

### Specialization Options

Valid values for `specialization` field:
- White Magic
- Black Magic
- Gray Magic
- Elemental Magic
- Time Magic
- Illusion Magic
- Healing Magic

---

## Request/Response Examples

### Complete Registration Flow

**1. Display Registration Form**
```bash
GET /users/register HTTP/1.1
Host: localhost:8080
```

Response: 200 OK with HTML form

**2. Submit Registration**
```bash
POST /users/register HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=abc123

username=wizard_john&email=john@magic.com&password=MagicPass123!&firstName=John&lastName=Wizard&specialization=White%20Magic&_csrf=token123
```

Response: 302 Found
Location: /users/success?id=1&name=John
Set-Cookie: JSESSIONID=def456; Path=/; HttpOnly

**3. Display Success Page**
```bash
GET /users/success?id=1&name=John HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=def456
```

Response: 200 OK with success confirmation page

**4. Access Home Page**
```bash
GET / HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=def456
```

Response: 200 OK with home page showing "Welcome, John!"

---

## Best Practices

### 1. Always Use Optional Safely
```java
userService.getUserById(1)
    .ifPresent(user -> System.out.println(user.getUsername()))
    .ifPresentOrElse(
        user -> processUser(user),
        () -> handleNotFound()
    );
```

### 2. Validate User Input
```java
if (!userService.usernameExists(username) && !userService.emailExists(email)) {
    user.setPassword(passwordEncoder.encode(plainPassword));
    userService.saveUser(user);
}
```

### 3. Handle Authentication in Controllers
```java
@GetMapping("/")
public String home(Authentication auth) {
    if (auth != null && auth.isAuthenticated()) {
        String username = auth.getName();
        // Process authenticated user
    } else {
        return "redirect:/login";
    }
}
```

### 4. Use Security Context Properly
```java
// Get current authenticated user
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
```

---

## Rate Limiting & Throttling

Currently not implemented. Consider adding for production deployments.

---

## Caching

Currently not implemented. Consider adding for frequently accessed data:
- User lookups by username/email
- All users list

---

## Monitoring & Logging

Add proper logging to production deployment:

```java
private static final Logger logger = LoggerFactory.getLogger(UserService.class);

public User saveUser(User user) {
    logger.info("Saving user with username: {}", user.getUsername());
    return userRepository.save(user);
}
```

---

Generated for Wizards Application v1.0
Last Updated: 2025-12-01