-- ============================================================================
-- QuoteVaultApp Database Schema for Supabase
-- ============================================================================
-- Run this script in Supabase SQL Editor to set up your database
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. PROFILES TABLE (extends auth.users)
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT,
    avatar_url TEXT,
    theme TEXT NOT NULL DEFAULT 'SYSTEM' CHECK (theme IN ('LIGHT', 'DARK', 'SYSTEM')),
    font_size TEXT NOT NULL DEFAULT 'MEDIUM' CHECK (font_size IN ('SMALL', 'MEDIUM', 'LARGE')),
    notification_time TIME,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create index on user_id for profiles
CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON public.profiles(id);

-- ============================================================================
-- 2. QUOTES TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.quotes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    text TEXT NOT NULL,
    author TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('MOTIVATION', 'LOVE', 'SUCCESS', 'WISDOM', 'HUMOR', 'GENERAL')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes on quotes for performance
CREATE INDEX IF NOT EXISTS idx_quotes_category ON public.quotes(category);
CREATE INDEX IF NOT EXISTS idx_quotes_created_at ON public.quotes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_quotes_author ON public.quotes(author);

-- ============================================================================
-- 3. USER_FAVORITES TABLE (junction table)
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.user_favorites (
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quote_id UUID NOT NULL REFERENCES public.quotes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, quote_id)
);

-- Create indexes on user_favorites for performance
CREATE INDEX IF NOT EXISTS idx_user_favorites_user_id ON public.user_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_user_favorites_quote_id ON public.user_favorites(quote_id);
CREATE INDEX IF NOT EXISTS idx_user_favorites_created_at ON public.user_favorites(created_at DESC);

-- ============================================================================
-- 4. COLLECTIONS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.collections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes on collections for performance
CREATE INDEX IF NOT EXISTS idx_collections_user_id ON public.collections(user_id);
CREATE INDEX IF NOT EXISTS idx_collections_created_at ON public.collections(created_at DESC);

-- ============================================================================
-- 5. COLLECTION_QUOTES TABLE (junction table)
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.collection_quotes (
    collection_id UUID NOT NULL REFERENCES public.collections(id) ON DELETE CASCADE,
    quote_id UUID NOT NULL REFERENCES public.quotes(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (collection_id, quote_id)
);

-- Create indexes on collection_quotes for performance
CREATE INDEX IF NOT EXISTS idx_collection_quotes_collection_id ON public.collection_quotes(collection_id);
CREATE INDEX IF NOT EXISTS idx_collection_quotes_quote_id ON public.collection_quotes(quote_id);
CREATE INDEX IF NOT EXISTS idx_collection_quotes_added_at ON public.collection_quotes(added_at DESC);

-- ============================================================================
-- TRIGGERS: Auto-update updated_at timestamps
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for profiles
CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for quotes
CREATE TRIGGER update_quotes_updated_at
    BEFORE UPDATE ON public.quotes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for collections
CREATE TRIGGER update_collections_updated_at
    BEFORE UPDATE ON public.collections
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.collections ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.collection_quotes ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- PROFILES POLICIES
-- ============================================================================

-- Users can view their own profile
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

-- Users can insert their own profile
CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- ============================================================================
-- QUOTES POLICIES
-- ============================================================================

-- Anyone can view quotes (public read access)
CREATE POLICY "Anyone can view quotes"
    ON public.quotes FOR SELECT
    USING (true);

-- Authenticated users can insert quotes
CREATE POLICY "Authenticated users can insert quotes"
    ON public.quotes FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Users can update their own quotes (if you add user_id later)
-- For now, only authenticated users can update
CREATE POLICY "Authenticated users can update quotes"
    ON public.quotes FOR UPDATE
    USING (auth.role() = 'authenticated')
    WITH CHECK (auth.role() = 'authenticated');

-- Authenticated users can delete quotes
CREATE POLICY "Authenticated users can delete quotes"
    ON public.quotes FOR DELETE
    USING (auth.role() = 'authenticated');

-- ============================================================================
-- USER_FAVORITES POLICIES
-- ============================================================================

-- Users can view their own favorites
CREATE POLICY "Users can view own favorites"
    ON public.user_favorites FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own favorites
CREATE POLICY "Users can insert own favorites"
    ON public.user_favorites FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can delete their own favorites
CREATE POLICY "Users can delete own favorites"
    ON public.user_favorites FOR DELETE
    USING (auth.uid() = user_id);

-- ============================================================================
-- COLLECTIONS POLICIES
-- ============================================================================

-- Users can view their own collections
CREATE POLICY "Users can view own collections"
    ON public.collections FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own collections
CREATE POLICY "Users can insert own collections"
    ON public.collections FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Users can update their own collections
CREATE POLICY "Users can update own collections"
    ON public.collections FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Users can delete their own collections
CREATE POLICY "Users can delete own collections"
    ON public.collections FOR DELETE
    USING (auth.uid() = user_id);

-- ============================================================================
-- COLLECTION_QUOTES POLICIES
-- ============================================================================

-- Users can view quotes in their own collections
CREATE POLICY "Users can view own collection quotes"
    ON public.collection_quotes FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.collections
            WHERE collections.id = collection_quotes.collection_id
            AND collections.user_id = auth.uid()
        )
    );

-- Users can insert quotes into their own collections
CREATE POLICY "Users can insert into own collections"
    ON public.collection_quotes FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.collections
            WHERE collections.id = collection_quotes.collection_id
            AND collections.user_id = auth.uid()
        )
    );

-- Users can delete quotes from their own collections
CREATE POLICY "Users can delete from own collections"
    ON public.collection_quotes FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM public.collections
            WHERE collections.id = collection_quotes.collection_id
            AND collections.user_id = auth.uid()
        )
    );

-- ============================================================================
-- FUNCTION: Automatically create profile when user signs up
-- ============================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, display_name, avatar_url, theme, font_size)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'display_name', NULL),
        COALESCE(NEW.raw_user_meta_data->>'avatar_url', NULL),
        COALESCE(NEW.raw_user_meta_data->>'theme', 'SYSTEM'),
        COALESCE(NEW.raw_user_meta_data->>'font_size', 'MEDIUM')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to automatically create profile on user signup
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
