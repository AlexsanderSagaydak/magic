
# Wizards Application - JavaDoc API Documentation

Complete documentation of all public methods in the Wizards application.

## Table of Contents
1. [Model Classes](#model-classes)
2. [Repository Interfaces](#repository-interfaces)
3. [Service Classes](#service-classes)
4. [Controller Classes](#controller-classes)
5. [Configuration Classes](#configuration-classes)

---

## Model Classes

### User Entity (`com.magic_fans.wizards.model.User`)

JPA entity representing a wizard user in the system. Implements Spring Security's `UserDetails` interface for authentication integration.

**Class-level Documentation:**
```
User entity class representing a wizard user in the system.
Implements Spring Security's UserDetails interface to integrate with authentication mechanism.

This class manages user profile information including authentication credentials,
personal details, and magical specialization. Users must be active to be enabled
for authentication.
```

#### Constructors

**`User()`**
- Default constructor for JPA/Hibernate entity instantiation
- No parameters
- Access: `public`

**`User(String username, String email, String password, String firstName, String lastName, String specialization)`**
- Constructor for creating a new User with all required fields
- Parameters:
  - `username` (String): The unique username for the user
  - `email` (String): The unique email address for the user
  - `password` (String): The encrypted password for authentication
  - `firstName` (String): The first name of the user
  - `lastName` (String): The last name of the user
  - `specialization` (String): The magical specialization (e.g., White Magic, Black Magic, etc.)
- Access: `public`
- Automatically sets `active` to `true`

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getId()` | int | Gets the unique identifier for the user |
| `setId(int id)` | void | Sets the unique identifier for the user |
| `getUsername()` | String | Gets the username of the user. Used for authentication purposes |
| `setUsername(String username)` | void | Sets the username of the user |
| `getEmail()` | String | Gets the email address of the user |
| `setEmail(String email)` | void | Sets the email address of the user |
| `getPassword()` | String | Gets the encrypted password of the user. Used for authentication purposes |
| `setPassword(String password)` | void | Sets the password of the user. Should be encrypted before storage |
| `getFirstName()` | String | Gets the first name of the user |
| `setFirstName(String firstName)` | void | Sets the first name of the user |
| `getLastName()` | String | Gets the last name of the user |
| `setLastName(String lastName)` | void | Sets the last name of the user |
| `getSpecialization()` | String | Gets the magical specialization of the user |
| `setSpecialization(String specialization)` | void | Sets the magical specialization of the user |
| `isActive()` | boolean | Checks if the user account is active. Active users can be enabled for authentication |
| `setActive(boolean active)` | void | Sets the active status of the user |
| `getAuthorities()` | Collection<? extends GrantedAuthority> | Returns the authorities granted to the user (implements UserDetails). All users receive ROLE_USER authority |
| `isAccountNonExpired()` | boolean | Indicates whether the user's account is non-expired (implements UserDetails) |
| `isAccountNonLocked()` | boolean | Indicates whether the user's account is non-locked (implements UserDetails) |
| `isCredentialsNonExpired()` | boolean | Indicates whether the user's credentials are non-expired (implements UserDetails) |
| `isEnabled()` | boolean | Indicates whether the user is enabled for authentication. Returns the value of `active` field |

---

## Repository Interfaces

### UserRepository (`com.magic_fans.wizards.repository.UserRepository`)

Spring Data JPA repository for User entity. Provides database access operations.

**Class-level Documentation:**
```
Repository interface for User entity providing data access operations.
Extends JpaRepository for standard CRUD operations and custom query methods.
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `findByUsername(String username)` | Optional<User> | Finds a user by username. Returns Optional containing the user if found, empty Optional otherwise |
| `findByEmail(String email)` | Optional<User> | Finds a user by email address. Returns Optional containing the user if found, empty Optional otherwise |
| `existsByUsername(String username)` | boolean | Checks if a user with the given username exists in the database |
| `existsByEmail(String email)` | boolean | Checks if a user with the given email exists in the database |

---

## Service Classes

### UserService (`com.magic_fans.wizards.service.UserService`)

Business logic service for user operations. Provides high-level operations for user management.

**Class-level Documentation:**
```
Service class for user business logic.
Provides operations for managing user accounts including creation, retrieval, update, and deletion.
Delegates database operations to UserRepository.
```

#### Public Methods

| Method | Return Type | Parameters | Description |
|--------|------------|-----------|-------------|
| `saveUser(User user)` | User | `User user` | Saves a new user or updates an existing user in the database. Returns the saved user with ID assigned |
| `getUserById(int id)` | Optional<User> | `int id` | Retrieves a user by their ID. Returns Optional containing the user if found |
| `getUserByUsername(String username)` | Optional<User> | `String username` | Retrieves a user by username. Returns Optional containing the user if found |
| `getUserByEmail(String email)` | Optional<User> | `String email` | Retrieves a user by email address. Returns Optional containing the user if found |
| `getAllUsers()` | List<User> | None | Retrieves all users from the database. Returns a list of all users |
| `deleteUser(int id)` | void | `int id` | Deletes a user by ID. Removes the user from the database |
| `usernameExists(String username)` | boolean | `String username` | Checks if a user with the given username exists. Returns true if exists, false otherwise |
| `emailExists(String email)` | boolean | `String email` | Checks if a user with the given email exists. Returns true if exists, false otherwise |
| `updateUser(User user)` | User | `User user` | Updates an existing user's information. Returns the updated user |

### CustomUserDetailsService (`com.magic_fans.wizards.service.CustomUserDetailsService`)

Spring Security UserDetailsService implementation for custom user loading logic.

**Class-level Documentation:**
```
Service class implementing Spring Security's UserDetailsService interface.
Loads user details from the database for authentication and authorization.
Throws UsernameNotFoundException if user is not found.
```

#### Public Methods

| Method | Return Type | Parameters | Exceptions | Description |
|--------|------------|-----------|-----------|-------------|
| `loadUserByUsername(String username)` | UserDetails | `String username` | `UsernameNotFoundException` | Loads user details by username for authentication. Returns the User object which implements UserDetails interface. Throws UsernameNotFoundException if user not found |

---

## Controller Classes

### UserController (`com.magic_fans.wizards.controller.UserController`)

Web controller handling user registration and profile operations.

**Request Mapping:** `/users`

**Class-level Documentation:**
```
Controller class for user registration and profile management endpoints.
Handles user registration with validation, password encoding, and automatic login.
Uses Spring Security for authentication and session management.
```

#### Public Methods

| HTTP Method | Endpoint | Method | Parameters | Returns | Description |
|-------------|----------|--------|-----------|---------|-------------|
| GET | `/users/register` | `showRegistrationForm(Model model)` | `Model model` | String ("register") | Displays the user registration form. Returns the registration template with an empty User object |
| POST | `/users/register` | `registerUser(User user, Model model, RedirectAttributes redirectAttributes, HttpSession session)` | `User user`, `Model model`, `RedirectAttributes redirectAttributes`, `HttpSession session` | String (redirect or template) | Registers a new user with validation. Validates that username and email are unique, encodes password, saves user, and auto-logs them in. Returns redirect to `/users/success` on success, or "register" template on error |
| GET | `/users/success` | `successPage(Integer id, String name, Model model)` | `Integer id` (optional), `String name` (optional), `Model model` | String ("success") | Displays registration success page. Accepts optional user ID and name parameters to display confirmation |
| GET | `/users/{id}` | `getUserProfile(int id, Model model)` | `int id` (path variable), `Model model` | String ("user-profile" or "error") | Retrieves and displays a user's profile page. Returns "user-profile" template with user data, or "error" if user not found |

### HomeController (`com.magic_fans.wizards.controller.HomeController`)

Web controller handling home page and login page navigation.

**Class-level Documentation:**
```
Controller class for home page and authentication-related endpoints.
Handles authentication checks and redirects authenticated/unauthenticated users appropriately.
Integrates with Spring Security for authentication verification.
```

#### Public Methods

| HTTP Method | Endpoint | Method | Parameters | Returns | Description |
|-------------|----------|--------|-----------|---------|-------------|
| GET | `/` | `home(Model model)` | `Model model` | String (template or redirect) | Home page endpoint. Checks if user is authenticated. If authenticated, returns "home" template with username. If not authenticated, redirects to "/login" |
| GET | `/login` | `login(Authentication auth)` | `Authentication auth` | String (template or redirect) | Login page endpoint. If user is already authenticated, redirects to "/". Otherwise returns "login" template for login form |

---

## Configuration Classes

### SecurityConfig (`com.magic_fans.wizards.config.SecurityConfig`)

Spring Security configuration for authentication and authorization.

**Class-level Documentation:**
```
Spring Security configuration class.
Configures password encoding, authentication providers, and HTTP security settings.
Enables form-based login with CSRF protection and custom user details service.
Defines authorization rules and session management policies.
```

#### Public Methods (Beans)

| Method | Return Type | Description |
|--------|-------------|-------------|
| `passwordEncoder()` | PasswordEncoder | Creates and returns a BCryptPasswordEncoder bean for password encoding. Uses the standard BCrypt algorithm with strength 10 |
| `authenticationProvider()` | DaoAuthenticationProvider | Creates a DaoAuthenticationProvider bean that uses the CustomUserDetailsService and PasswordEncoder for authentication |
| `authenticationManager(AuthenticationConfiguration config)` | AuthenticationManager | Creates an AuthenticationManager bean from the provided AuthenticationConfiguration. Used for manual authentication in controllers |
| `securityFilterChain(HttpSecurity http)` | SecurityFilterChain | Configures HTTP security settings. Defines authorization rules, form login configuration, CSRF protection, session management, and frame options for H2 console access |

**Security Configuration Details:**
- **Password Encoding**: BCrypt (strength 10)
- **Public Endpoints**: `/`, `/login`, `/users/register`, `/h2-console/**`
- **Protected Endpoints**: All other endpoints require authentication (ROLE_USER)
- **Session Policy**: Always create session (`SessionCreationPolicy.ALWAYS`)
- **Form Login**:
  - Login page: `/login`
  - Login processing URL: `/login`
  - Default success URL: `/`
  - Failure URL: `/login?error=true`
- **Logout**:
  - Logout URL: `/logout`
  - Success redirect: `/login`

### LocaleConfig (`com.magic_fans.wizards.config.LocaleConfig`)

Internationalization (i18n) configuration for language and locale support.

**Class-level Documentation:**
```
Configuration class for application localization.
Configures locale resolution and language change interceptor.
Supports multiple languages with cookie-based locale persistence.
```

#### Public Methods (Beans)

| Method | Return Type | Description |
|--------|-------------|-------------|
| `localeResolver()` | LocaleResolver | Creates a CookieLocaleResolver bean for storing user's language preference in cookies. Sets default locale to English |
| `localeChangeInterceptor()` | LocaleChangeInterceptor | Creates a LocaleChangeInterceptor bean that intercepts HTTP requests and changes locale based on "lang" request parameter |
| `addInterceptors(InterceptorRegistry registry)` | void | Registers the locale change interceptor with the application. Called automatically by Spring to add interceptors to the request pipeline |

**Localization Details:**
- **Supported Languages**: English (default), Russian
- **Locale Parameter**: `lang` (use `?lang=en` or `?lang=ru` to change language)
- **Storage Method**: HTTP cookies
- **Message Files**:
  - `messages.properties` (English)
  - `messages_ru.properties` (Russian)

---

## Additional Notes

### Authentication Flow
1. User registers via `/users/register` POST endpoint
2. Password is encoded using BCrypt before storage
3. User is automatically logged in after registration
4. Authentication token is stored in session (`SPRING_SECURITY_CONTEXT`)
5. User can access protected endpoints with ROLE_USER authority

### Internationalization (i18n)
The application supports multiple languages through Thymeleaf template integration:
- Users can change language using `?lang=en` or `?lang=ru` query parameter
- Language preference is stored in HTTP cookies
- All UI text is configurable through properties files

### Database
- **Type**: H2 in-memory database (for development)
- **Entities**: User (mapped to "users" table)
- **Access**: Spring Data JPA through UserRepository
- **Unique Constraints**: username and email fields are unique

### Security Features
- **Password Encoding**: BCrypt with strength 10
- **Session Management**: Always create session to ensure persistence
- **CSRF Protection**: Enabled for all POST/PUT/DELETE requests
- **User Roles**: All users receive ROLE_USER authority
- **Account Status**: Users must be active (active=true) to be enabled for authentication

---

## Code Examples

### User Registration Flow
```java
// Controller receives registration request
POST /users/register {username, email, password, firstName, lastName, specialization}

// Service validates and saves
userService.usernameExists(username)  // Check uniqueness
userService.emailExists(email)        // Check uniqueness
passwordEncoder.encode(password)      // Encrypt password
userService.saveUser(user)            // Save to database

// Auto-login
authenticationManager.authenticate(token)
session.setAttribute("SPRING_SECURITY_CONTEXT", context)

// Redirect to success
redirect: /users/success?id=1&name=John
```

### User Retrieval
```java
// Find by username
Optional<User> user = userService.getUserByUsername("john_doe");
user.ifPresent(u -> System.out.println(u.getEmail()));

// Find all users
List<User> allUsers = userService.getAllUsers();

// Check existence
boolean exists = userService.usernameExists("john_doe");
```

### Authentication
```java
// Spring Security automatically calls this for authentication
UserDetails userDetails = customUserDetailsService.loadUserByUsername("john_doe");
// Returns User object with ROLE_USER authority
```

---

Generated for Wizards Application v1.0