# Dependency Injection (DI)

The **DI layer** manages dependency injection using Hilt (Dagger Hilt). It provides all necessary dependencies to the application.

## Structure

- **AppModule**: Main Hilt module providing application-wide dependencies
- Additional modules can be created per feature or layer (e.g., DatabaseModule, NetworkModule)

## Responsibilities
- Provide dependencies using Hilt annotations (`@Module`, `@Provides`, `@Binds`)
- Manage dependency lifecycle and scoping
- Inject dependencies into classes using `@Inject` constructor injection
- Provide singleton instances for shared resources (Database, API clients, etc.)

## Hilt Annotations
- `@HiltAndroidApp`: Application class annotation
- `@Module`: Marks a class as a Hilt module
- `@InstallIn`: Specifies where the module is installed (Application, Activity, etc.)
- `@Provides`: Provides dependencies that can't be constructor-injected
- `@Binds`: Provides interface implementations
- `@Singleton`: Scopes a dependency to the application lifecycle
- `@Inject`: Constructor injection annotation

## Common Patterns
- Database: Provide Room database and DAOs
- Network: Provide Retrofit/Supabase clients
- Repository: Provide repository implementations
- Use Cases: Provide use case instances
- ViewModels: Automatically provided via `@HiltViewModel`
