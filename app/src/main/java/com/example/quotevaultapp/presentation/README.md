# Presentation Layer

The **Presentation Layer** handles all UI-related code and user interactions. It follows the MVVM (Model-View-ViewModel) pattern using Jetpack Compose.

## Structure

### Feature Folders
Each feature has its own folder containing:
- **ViewModel**: Manages UI-related data and business logic coordination
- **Screen**: Compose UI screens for the feature

#### `auth/`
- Authentication screens (login, signup, password reset)
- AuthViewModel manages authentication state and flows

#### `home/`
- Main home screen displaying quotes
- HomeViewModel coordinates quote fetching and display

#### `favorites/`
- Favorites/quotes saved by the user
- FavoritesViewModel manages favorite quotes

#### `collections/`
- User-created quote collections
- CollectionsViewModel handles collection CRUD operations

#### `profile/`
- User profile and account settings
- ProfileViewModel manages user profile data

#### `settings/`
- App settings and preferences
- SettingsViewModel handles app configuration

### `components/`
- Reusable Compose UI components used across features
- Examples: QuoteCard, LoadingIndicator, ErrorMessage

## Responsibilities
- Display UI using Jetpack Compose
- Handle user interactions and input
- Manage UI state through ViewModels
- Navigate between screens
- Observe data from ViewModels using StateFlow/LiveData
- Map domain models to UI state models

## MVVM Pattern
- **Model**: Domain layer models
- **View**: Compose screens (UI)
- **ViewModel**: Bridge between View and Domain layer, manages UI state

## Best Practices
- ViewModels should not contain Android UI dependencies (Context, View, etc.)
- Use StateFlow or LiveData for observable state
- Keep screens (Composables) as stateless as possible
- Extract reusable UI logic into components
- Handle loading, error, and success states properly
