# Database Schema Setup Guide

This directory contains the SQL schema for the QuoteVaultApp Supabase database.

## Quick Start

1. Log in to your [Supabase Dashboard](https://app.supabase.com)
2. Select your project
3. Go to **SQL Editor**
4. Click **New Query**
5. Copy and paste the contents of `schema.sql`
6. Click **Run** (or press `Ctrl+Enter` / `Cmd+Enter`)

## Schema Overview

### Tables

1. **profiles** - User profile information (extends `auth.users`)
   - Stores user preferences (theme, font size, etc.)
   - Automatically created when a user signs up

2. **quotes** - Quote collection
   - Public read access
   - Authenticated users can create, update, and delete

3. **user_favorites** - Junction table for user favorite quotes
   - Users can only manage their own favorites

4. **collections** - User-created quote collections
   - Users can create multiple collections
   - Users can only access their own collections

5. **collection_quotes** - Junction table linking quotes to collections
   - Many-to-many relationship between collections and quotes

## Features

### Row Level Security (RLS)
All tables have RLS enabled with policies that:
- Ensure users can only access their own data
- Allow public read access to quotes
- Restrict write operations to authenticated users

### Indexes
Performance indexes are created on:
- Foreign keys for fast joins
- Frequently queried columns (category, created_at, etc.)
- Search fields (author, etc.)

### Triggers
- **Auto-update timestamps**: `updated_at` fields are automatically updated on record modification
- **Auto-create profiles**: Profile is automatically created when a user signs up via Supabase Auth

## Verification

After running the schema, verify the setup:

```sql
-- Check that all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Check RLS is enabled
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public';

-- Check indexes
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public' 
ORDER BY tablename, indexname;
```

## Troubleshooting

### "permission denied for schema public"
- Make sure you're running the SQL as a database administrator
- Supabase projects have admin access by default

### "relation already exists"
- The schema uses `CREATE TABLE IF NOT EXISTS`, so it's safe to run multiple times
- If you need to recreate tables, drop them first with `DROP TABLE IF EXISTS`

### RLS Policies not working
- Verify RLS is enabled: `SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname = 'public';`
- Check that policies exist: `SELECT * FROM pg_policies WHERE schemaname = 'public';`
- Ensure you're authenticated when testing policies

## Next Steps

After setting up the schema:

1. Test user registration - a profile should be automatically created
2. Insert some sample quotes to test the application
3. Test RLS policies by creating test users and verifying access restrictions
4. Configure any additional policies as needed for your use case

## Sample Data (Optional)

You can add sample quotes for testing:

```sql
INSERT INTO public.quotes (text, author, category) VALUES
('The only way to do great work is to love what you do.', 'Steve Jobs', 'MOTIVATION'),
('Life is what happens to you while you''re busy making other plans.', 'John Lennon', 'WISDOM'),
('The future belongs to those who believe in the beauty of their dreams.', 'Eleanor Roosevelt', 'SUCCESS');
```
