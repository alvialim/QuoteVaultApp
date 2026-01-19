# QuoteVault 📚

A beautiful Android app for discovering, saving, and sharing inspirational quotes.

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/[yourusername]/QuoteVaultApp.git
   cd QuoteVaultApp
   ```

2. **Set up Supabase**
   - Create a project at [supabase.com](https://supabase.com)
   - Copy your URL and anon key
   - Add to `local.properties`:
     ```properties
     SUPABASE_URL=https://your-project.supabase.co
     SUPABASE_ANON_KEY=your-anon-key-here
     ```

3. **Initialize Database**
   - Run `docs/supabase-schema.sql` in Supabase SQL Editor
   - Optionally run `docs/supabase-seed.sql` for sample data

4. **Build & Run**
   - Open in Android Studio
   - Sync Gradle
   - Run on device/emulator

## Features

- ✅ Email/password authentication
- ✅ Browse quotes by category (Motivation, Love, Success, Wisdom, Humor)
- ✅ Search quotes and authors
- ✅ Save favorites (syncs across devices)
- ✅ Create custom collections
- ✅ Daily quote notifications
- ✅ Share quotes as text or beautiful images
- ✅ Dark mode & theme customization
- ✅ Android home screen widget

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **Backend**: Supabase (Auth, Postgrest)
- **Local Storage**: DataStore Preferences
- **Background Tasks**: WorkManager

## Package Name

`com.example.quotevaultapp`

## Design System

Built with [Google Stitch Design System](https://stitch.withgoogle.com/projects/11692299594384479929)

## Screenshots

Screenshots available in `app/screenshots/`:
- `login_screen.png`
- `home_screen.png`
- `favorite_screen.png`
- `collection_screen.png`
- `settings_screen.png`

## Documentation

- **Full README**: See [README.md](README.md)
- **Supabase Setup**: See [README-SUPABASE-SETUP.md](README-SUPABASE-SETUP.md)
- **Database Schema**: See [docs/supabase-schema.sql](docs/supabase-schema.sql)
- **Seed Data**: See [docs/supabase-seed.sql](docs/supabase-seed.sql)

## License

MIT License - see [LICENSE](LICENSE) for details

---

**Built with ❤️ using Kotlin, Jetpack Compose, and Supabase**
