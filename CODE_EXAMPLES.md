# Wizards Application - Code Examples & Patterns

Practical code examples demonstrating how to use the application's API and best practices.

---

## Table of Contents
1. [Model Usage Examples](#model-usage-examples)
2. [Service Layer Examples](#service-layer-examples)
3. [Controller Examples](#controller-examples)
4. [Repository Examples](#repository-examples)
5. [Security Examples](#security-examples)
6. [Common Patterns](#common-patterns)
7. [Error Handling Examples](#error-handling-examples)

---

## Model Usage Examples

### Creating a User Object

```java
// Method 1: Using constructor with all fields
User user = new User(
    "wizard_alice",
    "alice@magic.com",
    "EncodedPassword123",
    "Alice",
    "Wonder",
    "Black Magic"
);

// Method 2: Using default constructor and setters
User user = new User();
user.setUsername("wizard_bob");
user.setEmail("bob@magic.com");
user.setPassword("EncodedPassword456");
user.setFirstName("Bob");
user.setLastName("Smith");
user.setSpecialization("Elemental Magic");
user.setActive(true);
```

### Accessing User Properties

```java
User user = userService.getUserByUsername("wizard_alice").orElse(null);

if (user != null) {
    // Get user information
    int userId = user.getId();
    String username = user.getUsername();
    String email = user.getEmail();
    String firstName = user.getFirstName();
    String lastName = user.getLastName();
    String specialization = user.getSpecialization();
    boolean isActive = user.isActive();

    // Get security information
    Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
    boolean isEnabled = user.isEnabled();
    boolean isNonExpired = user.isAccountNonExpired();
    boolean isNonLocked = user.isAccountNonLocked();
}
```

### Modifying User Information

```java
// Retrieve user
Optional<User> userOpt = userService.getUserByUsername("wizard_alice");

if (userOpt.isPresent()) {
    User user = userOpt.get();

    // Modify user information
    user.setFirstName("Alicia");
    user.setLastName("Wonderland");
    user.setSpecialization("White Magic");

    // Save changes
    User updatedUser = userService.updateUser(user);
    System.out.println("User updated: " + updatedUser.getFirstName());
}
```

### Deactivating User Account

```java
// Retrieve user
User user = userService.getUserById(1).orElse(null);

if (user != null) {
    // Deactivate account
    user.setActive(false);
    userService.updateUser(user);

    // After deactivation:
    // - user.isEnabled() returns false
    // - User cannot authenticate
    // - user.isActive() returns false
}
```

---

## Service Layer Examples

### UserService Usage Patterns

```java
@Service
public class MyCustomService {

    @Autowired
    private UserService userService;

    // Example 1: Create and save new user
    public void createNewWizard(String username, String email, String password,
                               String firstName, String lastName, String specialization) {
        // Check if user already exists
        if (userService.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userService.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User newUser = new User(username, email, password, firstName, lastName, specialization);
        User savedUser = userService.saveUser(newUser);
        System.out.println("New user created with ID: " + savedUser.getId());
    }

    // Example 2: Find and process user
    public void processUserByUsername(String username) {
        userService.getUserByUsername(username)
            .ifPresentOrElse(
                user -> {
                    System.out.println("Processing user: " + user.getFirstName());
                    // Process user logic here
                    if (user.isActive()) {
                        System.out.println("User is active");
                    }
                },
                () -> System.out.println("User not found")
            );
    }

    // Example 3: List and filter users
    public List<String> getActiveWizards() {
        return userService.getAllUsers()
            .stream()
            .filter(User::isActive)
            .map(u -> u.getFirstName() + " " + u.getLastName())
            .collect(Collectors.toList());
    }

    // Example 4: Update user specialization
    public void updateSpecialization(int userId, String newSpecialization) {
        userService.getUserById(userId)
            .ifPresent(user -> {
                user.setSpecialization(newSpecialization);
                userService.updateUser(user);
                System.out.println("Specialization updated to: " + newSpecialization);
            });
    }

    // Example 5: Delete user safely
    public boolean removeUser(int userId) {
        if (userService.getUserById(userId).isPresent()) {
            userService.deleteUser(userId);
            return true;
        }
        return false;
    }
}
```

---

## Controller Examples

### Custom User Management Controller

```java
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // List all users
    @GetMapping("")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        return "admin/user-list";
    }

    // View user details
    @GetMapping("/{id}")
    public String viewUser(@PathVariable int id, Model model) {
        return userService.getUserById(id)
            .map(user -> {
                model.addAttribute("user", user);
                model.addAttribute("isActive", user.isActive());
                return "admin/user-detail";
            })
            .orElse("admin/error-user-not-found");
    }

    // Edit user
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable int id, Model model) {
        return userService.getUserById(id)
            .map(user -> {
                model.addAttribute("user", user);
                return "admin/user-edit";
            })
            .orElse("admin/error-user-not-found");
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable int id,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String specialization,
                           RedirectAttributes redirectAttributes) {
        userService.getUserById(id)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setSpecialization(specialization);
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("message", "User updated successfully");
            });
        return "redirect:/admin/users/" + id;
    }

    // Deactivate user
    @PostMapping("/{id}/deactivate")
    public String deactivateUser(@PathVariable int id, RedirectAttributes redirectAttributes) {
        userService.getUserById(id)
            .ifPresent(user -> {
                user.setActive(false);
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("message", "User deactivated");
            });
        return "redirect:/admin/users";
    }

    // Delete user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully");
        }
        return "redirect:/admin/users";
    }
}
```

### Authentication-Aware Controller

```java
@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService userService;

    // Get current user's profile
    @GetMapping("")
    public String myProfile(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            userService.getUserByUsername(username)
                .ifPresent(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("message",
                        "Welcome " + user.getFirstName() + "!");
                });
        }
        return "profile";
    }

    // Update current user's profile
    @PostMapping("/update")
    public String updateProfile(Authentication auth,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               RedirectAttributes redirectAttributes) {
        String username = auth.getName();
        userService.getUserByUsername(username)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("message",
                    "Profile updated successfully");
            });
        return "redirect:/profile";
    }
}
```

---

## Repository Examples

### Direct Repository Access

```java
@Service
public class AdvancedUserQueries {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Example 1: Check unique username/email combination
    public boolean isUserRegistered(String username, String email) {
        return userRepository.existsByUsername(username) ||
               userRepository.existsByEmail(email);
    }

    // Example 2: Get user safely
    public User getUserOrNull(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Example 3: Find user and process
    public String getUserSpecialization(String email) {
        return userRepository.findByEmail(email)
            .map(User::getSpecialization)
            .orElse("Unknown");
    }

    // Example 4: CRUD operations
    public void crudExamples() {
        // Create
        User newUser = new User("user", "user@test.com", "pwd",
                               "First", "Last", "White Magic");
        User saved = userRepository.save(newUser);
        int newId = saved.getId();

        // Read
        User retrieved = userRepository.findById(newId).orElse(null);

        // Update
        if (retrieved != null) {
            retrieved.setSpecialization("Black Magic");
            userRepository.save(retrieved);
        }

        // Delete
        userRepository.deleteById(newId);
    }

    // Example 5: Get all users and filter
    public List<User> getAllActiveUsers() {
        return userRepository.findAll()
            .stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
    }

    // Example 6: Find users by specialization
    public List<String> getUsersBySpecialization(String specialization) {
        return userRepository.findAll()
            .stream()
            .filter(u -> u.getSpecialization().equals(specialization))
            .map(User::getUsername)
            .collect(Collectors.toList());
    }
}
```

---

## Security Examples

### Password Management

```java
@Service
public class PasswordManagementService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    // Example 1: Encode password for storage
    public String encodeNewPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    // Example 2: Verify password
    public boolean verifyPassword(String plainPassword, String encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword);
    }

    // Example 3: Change user password
    public boolean changeUserPassword(int userId, String newPassword) {
        return userService.getUserById(userId)
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.updateUser(user);
                return true;
            })
            .orElse(false);
    }

    // Example 4: Safe password update with old password verification
    public boolean securePasswordChange(String username,
                                       String oldPassword,
                                       String newPassword) {
        Optional<User> userOpt = userService.getUserByUsername(username);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;  // Old password incorrect
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        return true;
    }
}
```

### Authentication Context Usage

```java
@Service
public class SecurityContextExamples {

    // Example 1: Get current authenticated user
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }

    // Example 2: Check if user has specific role
    public boolean userHasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals(role));
    }

    // Example 3: Get user details from context
    @Autowired
    private UserService userService;

    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            return userService.getUserByUsername(username).orElse(null);
        }
        return null;
    }

    // Example 4: Check if user is authenticated
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() &&
               !auth.getPrincipal().equals("anonymousUser");
    }
}
```

---

## Common Patterns

### Optional Handling Patterns

```java
// Pattern 1: ifPresent
userService.getUserById(1)
    .ifPresent(user -> System.out.println(user.getUsername()));

// Pattern 2: ifPresentOrElse
userService.getUserById(1)
    .ifPresentOrElse(
        user -> System.out.println("Found: " + user.getUsername()),
        () -> System.out.println("User not found")
    );

// Pattern 3: map and filter
String email = userService.getUserById(1)
    .map(User::getEmail)
    .filter(e -> e.contains("@"))
    .orElse("no-email");

// Pattern 4: orElseGet with computation
User user = userService.getUserById(1)
    .orElseGet(() -> createDefaultUser());

// Pattern 5: orElseThrow
User user = userService.getUserById(1)
    .orElseThrow(() -> new UserNotFoundException("User not found"));
```

### Stream Processing Patterns

```java
@Service
public class StreamExamples {

    @Autowired
    private UserService userService;

    // Filter and map
    public List<String> getActiveUserNames() {
        return userService.getAllUsers()
            .stream()
            .filter(User::isActive)
            .map(u -> u.getFirstName() + " " + u.getLastName())
            .collect(Collectors.toList());
    }

    // Group by specialization
    public Map<String, List<User>> getUsersBySpecialization() {
        return userService.getAllUsers()
            .stream()
            .collect(Collectors.groupingBy(User::getSpecialization));
    }

    // Count by condition
    public long countActiveUsers() {
        return userService.getAllUsers()
            .stream()
            .filter(User::isActive)
            .count();
    }

    // Check if any user matches condition
    public boolean hasUserWithEmail(String email) {
        return userService.getAllUsers()
            .stream()
            .anyMatch(u -> u.getEmail().equals(email));
    }

    // Collect to custom object
    public List<UserDTO> getUserDTOs() {
        return userService.getAllUsers()
            .stream()
            .map(user -> new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName() + " " + user.getLastName()
            ))
            .collect(Collectors.toList());
    }
}
```

---

## Error Handling Examples

### Service Layer Error Handling

```java
@Service
public class UserRegistrationService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Example 1: Validation with custom exceptions
    public User registerUser(UserRegistrationDTO dto)
            throws InvalidRegistrationException {

        // Validate username uniqueness
        if (userService.usernameExists(dto.getUsername())) {
            throw new InvalidRegistrationException(
                "Username '" + dto.getUsername() + "' is already taken"
            );
        }

        // Validate email uniqueness
        if (userService.emailExists(dto.getEmail())) {
            throw new InvalidRegistrationException(
                "Email '" + dto.getEmail() + "' is already registered"
            );
        }

        // Validate password strength
        if (!isPasswordStrong(dto.getPassword())) {
            throw new InvalidRegistrationException(
                "Password does not meet strength requirements"
            );
        }

        // Create and save user
        User user = new User(
            dto.getUsername(),
            dto.getEmail(),
            passwordEncoder.encode(dto.getPassword()),
            dto.getFirstName(),
            dto.getLastName(),
            dto.getSpecialization()
        );

        return userService.saveUser(user);
    }

    // Example 2: Try-catch for database operations
    public User updateUserSafely(User user) {
        try {
            return userService.updateUser(user);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return null;
        }
    }

    // Example 3: Optional for safe retrieval
    public User getOrCreateUser(String username, String email) {
        return userService.getUserByUsername(username)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(email);
                return userService.saveUser(newUser);
            });
    }

    private boolean isPasswordStrong(String password) {
        // At least 8 characters, contain numbers and letters
        return password.length() >= 8 &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[a-zA-Z].*");
    }
}
```

### Controller Error Handling

```java
@Controller
public class UserErrorHandlingController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/{id}")
    public String getUserProfile(@PathVariable int id, Model model) {
        return userService.getUserById(id)
            .map(user -> {
                model.addAttribute("user", user);
                return "user-profile";
            })
            .orElseGet(() -> {
                model.addAttribute("error", "User with ID " + id + " not found");
                return "error/404";
            });
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable int id,
                           @RequestParam String firstName,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            if (firstName == null || firstName.trim().isEmpty()) {
                model.addAttribute("error", "First name cannot be empty");
                return "user-edit";
            }

            userService.getUserById(id)
                .ifPresentOrElse(
                    user -> {
                        user.setFirstName(firstName);
                        userService.updateUser(user);
                        redirectAttributes.addFlashAttribute("message",
                            "User updated successfully");
                    },
                    () -> {
                        model.addAttribute("error", "User not found");
                    }
                );
        } catch (Exception e) {
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            return "user-edit";
        }

        return "redirect:/users/" + id;
    }
}
```

---

## Tips & Best Practices

1. **Always use Optional safely** - Don't call `.get()` without checking
2. **Validate input data** - Check for null, empty strings, valid formats
3. **Handle exceptions appropriately** - Catch specific exceptions
4. **Use meaningful variable names** - Makes code self-documenting
5. **Log important operations** - Helps with debugging
6. **Follow DRY principle** - Don't repeat code
7. **Write testable code** - Dependency injection helps
8. **Use streams for collections** - More readable than loops
9. **Cache frequently accessed data** - Improves performance
10. **Keep services focused** - Single responsibility principle

---

Generated for Wizards Application v1.0