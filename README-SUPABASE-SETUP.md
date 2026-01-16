# Supabase Setup Guide

This guide will help you configure Supabase for the QuoteVaultApp project.

## Prerequisites

1. A Supabase account (create one at [supabase.com](https://supabase.com))
2. A Supabase project created

## Step 1: Get Your Supabase Credentials

1. Log in to your [Supabase Dashboard](https://app.supabase.com)
2. Select your project (or create a new one)
3. Go to **Settings** → **API**
4. Copy the following values:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (under "Project API keys")

## Step 2: Configure local.properties

1. Open the `local.properties` file in the root of the project
2. Add your Supabase credentials:

```properties
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

**Important:** 
- Replace `your-project-id` with your actual Supabase project reference ID
- Replace `your-anon-key-here` with your actual anon/public key
- The `local.properties` file is already in `.gitignore` and will NOT be committed to version control

## Step 3: Verify Build Configuration

The `app/build.gradle.kts` file is already configured to:
- Enable `buildConfig = true` for generating BuildConfig fields
- Read `SUPABASE_URL` and `SUPABASE_ANON_KEY` from `local.properties`
- Inject them as BuildConfig constants at compile time

## Step 4: Using the Supabase Client

The Supabase client is available as a singleton via `SupabaseClient`:

```kotlin
import com.example.quotevaultapp.data.remote.supabase.SupabaseClient

// Access Auth
val auth = SupabaseClient.auth

// Access Postgrest (database)
val postgrest = SupabaseClient.postgrest

// Access Realtime
val realtime = SupabaseClient.realtime

// Or access the full client
val client = SupabaseClient.client
```

## Step 5: Database Setup (Optional)

After configuring your credentials, you may need to:

1. Set up database tables in Supabase Dashboard
2. Configure Row Level Security (RLS) policies
3. Set up authentication providers if needed

### Example: Creating a quotes table

In Supabase SQL Editor, run:

```sql
CREATE TABLE quotes (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  text TEXT NOT NULL,
  author TEXT NOT NULL,
  category TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  is_favorite BOOLEAN DEFAULT false,
  user_id UUID REFERENCES auth.users(id)
);

-- Enable Row Level Security
ALTER TABLE quotes ENABLE ROW LEVEL SECURITY;

-- Create policy for authenticated users
CREATE POLICY "Users can view all quotes"
  ON quotes FOR SELECT
  USING (true);

CREATE POLICY "Users can insert their own quotes"
  ON quotes FOR INSERT
  WITH CHECK (auth.uid() = user_id);
```

## Security Best Practices

✅ **DO:**
- Keep `local.properties` in `.gitignore`
- Use the anon/public key (safe for client-side use)
- Use Row Level Security (RLS) policies in Supabase
- Validate user input before sending to Supabase

❌ **DON'T:**
- Commit API keys to version control
- Use service_role key in client-side code
- Disable RLS without proper authentication
- Hardcode credentials in source code

## Troubleshooting

### BuildConfig fields are empty

1. Ensure `local.properties` exists in the project root
2. Verify the property names are exactly `SUPABASE_URL` and `SUPABASE_ANON_KEY`
3. Sync Gradle files after modifying `local.properties`
4. Clean and rebuild the project

### "SUPABASE_URL must be configured" error

This error occurs when the BuildConfig fields are empty. Ensure:
- `local.properties` contains the correct properties
- You've synced Gradle after adding the properties
- The properties don't have extra quotes or spaces

### Connection errors

- Verify your Supabase project is active
- Check if your project URL is correct
- Ensure you're using the anon/public key, not the service_role key
- Check network connectivity and firewall settings

## Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin SDK](https://github.com/supabase-community/supabase-kt)
- [Supabase Auth Guide](https://supabase.com/docs/guides/auth)
- [Supabase Realtime Guide](https://supabase.com/docs/guides/realtime)

## Need Help?

If you encounter issues:
1. Check the [Supabase Status Page](https://status.supabase.com)
2. Review Supabase logs in your dashboard
3. Check Android Studio's Build output for detailed error messages
