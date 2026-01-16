# Data Layer

The **Data Layer** is responsible for managing data sources and implementing the data access logic. It contains implementations of repositories defined in the domain layer.

## Structure

### `local/`
- **`dao/`**: Data Access Objects (DAOs) for Room database operations
- **`database/`**: Room database configuration and setup

### `remote/`
- **`supabase/`**: Supabase client configuration and API service interfaces for remote data operations

### `repository/`
- Repository implementations that bridge between local (Room) and remote (Supabase) data sources
- Implements the interfaces defined in `domain/repository/`

### `model/`
- Data layer models (entities, DTOs, API response models)
- These models are specific to data sources (Room entities, API responses)
- Should be mapped to domain models when passing data to the domain layer

## Responsibilities
- Manage data sources (local database, remote API)
- Implement repository interfaces from the domain layer
- Handle data mapping between data models and domain models
- Provide data caching and offline support
- Handle data synchronization between local and remote sources
