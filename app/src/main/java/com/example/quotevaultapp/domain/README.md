# Domain Layer

The **Domain Layer** is the core of the application and contains business logic. It is independent of frameworks and data sources, making it testable and maintainable.

## Structure

### `model/`
- Pure Kotlin data classes representing business entities
- No Android, Room, or Supabase dependencies
- These models are used across the entire application

### `repository/`
- Repository interfaces that define data contracts
- Declare methods that the data layer must implement
- Represent the "what" (contracts) not the "how" (implementation)

### `usecase/`
- Use cases encapsulate specific business logic operations
- Each use case represents a single business action (e.g., GetQuotesUseCase, SaveFavoriteUseCase)
- Coordinate between repositories to perform business operations
- Should be focused and single-purpose

## Responsibilities
- Define business logic and rules
- Provide repository interfaces (contracts)
- Implement use cases for specific business operations
- Remain framework-independent (no Android dependencies)
- Act as the single source of truth for business logic

## Benefits
- **Testability**: Can be unit tested without Android dependencies
- **Reusability**: Business logic can be reused across different platforms
- **Maintainability**: Changes to UI or data sources don't affect business logic
- **Clean Architecture**: Central layer that other layers depend on
