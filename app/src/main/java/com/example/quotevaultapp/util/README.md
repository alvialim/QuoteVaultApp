# Utilities

The **Util** layer contains utility functions, extensions, and constants used across the application.

## Structure

- **Extensions.kt**: Kotlin extension functions for common operations
- **Constants.kt**: Application-wide constants (API endpoints, keys, etc.)

## Responsibilities
- Provide reusable utility functions
- Define extension functions for common operations
- Store application constants and configuration values
- Provide helper functions for data formatting, validation, etc.

## Common Utilities

### Extensions
- String extensions (formatting, validation)
- Date/Time extensions
- View/Composable extensions
- Collection extensions
- Flow/StateFlow extensions

### Constants
- API endpoints and URLs
- Database names and versions
- SharedPreferences keys
- Default values
- Configuration values

## Best Practices
- Keep utilities pure (no side effects when possible)
- Document utility functions clearly
- Group related utilities together
- Avoid putting business logic in utilities
- Use extension functions to enhance readability
