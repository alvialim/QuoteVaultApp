# Navigation

The **Navigation** layer manages app navigation using Jetpack Navigation Compose.

## Structure

- **Screen.kt**: Defines screen routes as sealed classes or objects
- **NavGraph.kt**: Configures the navigation graph with all routes and destinations

## Responsibilities
- Define all screen routes in the app
- Configure navigation graph with destinations
- Handle navigation arguments and deep links
- Manage navigation state and back stack
- Integrate with Hilt for ViewModel injection

## Common Patterns

### Screen Routes
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    // ...
}
```

### Navigation Graph
- Uses `NavHost` and `NavController`
- Connects routes to Composable destinations
- Handles navigation arguments and type safety

## Best Practices
- Use type-safe navigation where possible
- Define all routes in a single location
- Use sealed classes or objects for route definitions
- Handle navigation arguments safely
- Support deep linking for important screens
