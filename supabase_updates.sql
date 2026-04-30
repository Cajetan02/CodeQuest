-- CodeQuest Supabase Schema Updates for New Features

-- 1. Add new question types to the ENUM if possible.
-- PostgreSQL doesn't allow adding values to an enum type inside a transaction block easily, 
-- but here's the standard way:
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'true_false';
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'code_snippet';
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'matching';
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'word_bank';
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'listen_type';
ALTER TYPE question_type ADD VALUE IF NOT EXISTS 'tap_pairs';

-- 2. Add avatar_url to user_stats table to support profile pictures
ALTER TABLE user_stats 
ADD COLUMN IF NOT EXISTS avatar_url TEXT;

-- 3. Enhance questions table to support new question types
ALTER TABLE questions
ADD COLUMN IF NOT EXISTS code_snippet TEXT,
ADD COLUMN IF NOT EXISTS explanation TEXT;

-- 4. Create an achievements table if it doesn't exist, to support outstanding features later
CREATE TABLE IF NOT EXISTS achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    unlock_criteria TEXT NOT NULL,
    icon_url TEXT
);

-- 5. Create a user_achievements mapping table
CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    achievement_id UUID REFERENCES achievements(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, achievement_id)
);

-- 6. Add Row Level Security (RLS) to new tables
ALTER TABLE achievements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Achievements are viewable by everyone" ON achievements FOR SELECT USING (true);

ALTER TABLE user_achievements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view their own achievements" ON user_achievements FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own achievements" ON user_achievements FOR INSERT WITH CHECK (auth.uid() = user_id);