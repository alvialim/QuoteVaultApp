-- ============================================================================
-- QuoteVaultApp - Seed Quotes Data
-- ============================================================================
-- Run this script in Supabase SQL Editor after running schema.sql
-- Contains 100+ famous quotes across all categories
-- ============================================================================

-- ============================================================================
-- MOTIVATION QUOTES (20+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'The only way to do great work is to love what you do.', 'Steve Jobs', 'MOTIVATION'),
(gen_random_uuid(), 'Believe you can and you''re halfway there.', 'Theodore Roosevelt', 'MOTIVATION'),
(gen_random_uuid(), 'The future belongs to those who believe in the beauty of their dreams.', 'Eleanor Roosevelt', 'MOTIVATION'),
(gen_random_uuid(), 'It does not matter how slowly you go as long as you do not stop.', 'Confucius', 'MOTIVATION'),
(gen_random_uuid(), 'Success is not final, failure is not fatal: it is the courage to continue that counts.', 'Winston Churchill', 'MOTIVATION'),
(gen_random_uuid(), 'The only impossible journey is the one you never begin.', 'Tony Robbins', 'MOTIVATION'),
(gen_random_uuid(), 'Don''t watch the clock; do what it does. Keep going.', 'Sam Levenson', 'MOTIVATION'),
(gen_random_uuid(), 'The only person you are destined to become is the person you decide to be.', 'Ralph Waldo Emerson', 'MOTIVATION'),
(gen_random_uuid(), 'Go confidently in the direction of your dreams. Live the life you have imagined.', 'Henry David Thoreau', 'MOTIVATION'),
(gen_random_uuid(), 'The way to get started is to quit talking and begin doing.', 'Walt Disney', 'MOTIVATION'),
(gen_random_uuid(), 'If you can dream it, you can do it.', 'Walt Disney', 'MOTIVATION'),
(gen_random_uuid(), 'What lies behind us and what lies before us are tiny matters compared to what lies within us.', 'Ralph Waldo Emerson', 'MOTIVATION'),
(gen_random_uuid(), 'You are never too old to set another goal or to dream a new dream.', 'C.S. Lewis', 'MOTIVATION'),
(gen_random_uuid(), 'Keep your face always toward the sunshine—and shadows will fall behind you.', 'Walt Whitman', 'MOTIVATION'),
(gen_random_uuid(), 'It is during our darkest moments that we must focus to see the light.', 'Aristotle', 'MOTIVATION'),
(gen_random_uuid(), 'The only limit to our realization of tomorrow will be our doubts of today.', 'Franklin D. Roosevelt', 'MOTIVATION'),
(gen_random_uuid(), 'You miss 100% of the shots you don''t take.', 'Wayne Gretzky', 'MOTIVATION'),
(gen_random_uuid(), 'Act as if what you do makes a difference. It does.', 'William James', 'MOTIVATION'),
(gen_random_uuid(), 'Do what you can, with what you have, where you are.', 'Theodore Roosevelt', 'MOTIVATION'),
(gen_random_uuid(), 'The best time to plant a tree was 20 years ago. The second best time is now.', 'Chinese Proverb', 'MOTIVATION'),
(gen_random_uuid(), 'Dream big and dare to fail.', 'Norman Vaughan', 'MOTIVATION'),
(gen_random_uuid(), 'The secret of getting ahead is getting started.', 'Mark Twain', 'MOTIVATION');

-- ============================================================================
-- LOVE QUOTES (20+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'The best thing to hold onto in life is each other.', 'Audrey Hepburn', 'LOVE'),
(gen_random_uuid(), 'Love is composed of a single soul inhabiting two bodies.', 'Aristotle', 'LOVE'),
(gen_random_uuid(), 'Being deeply loved by someone gives you strength, while loving someone deeply gives you courage.', 'Lao Tzu', 'LOVE'),
(gen_random_uuid(), 'The course of true love never did run smooth.', 'William Shakespeare', 'LOVE'),
(gen_random_uuid(), 'Love recognizes no barriers. It jumps hurdles, leaps fences, penetrates walls to arrive at its destination full of hope.', 'Maya Angelou', 'LOVE'),
(gen_random_uuid(), 'Love is not finding someone to live with. It''s finding someone you can''t live without.', 'Rafael Ortiz', 'LOVE'),
(gen_random_uuid(), 'The best and most beautiful things in this world cannot be seen or even heard, but must be felt with the heart.', 'Helen Keller', 'LOVE'),
(gen_random_uuid(), 'Love is friendship that has caught fire.', 'Ann Landers', 'LOVE'),
(gen_random_uuid(), 'In all the world, there is no heart for me like yours. In all the world, there is no love for you like mine.', 'Maya Angelou', 'LOVE'),
(gen_random_uuid(), 'Love isn''t something you find. Love is something that finds you.', 'Loretta Young', 'LOVE'),
(gen_random_uuid(), 'I have decided to stick with love. Hate is too great a burden to bear.', 'Martin Luther King Jr.', 'LOVE'),
(gen_random_uuid(), 'The heart wants what it wants.', 'Emily Dickinson', 'LOVE'),
(gen_random_uuid(), 'True love is rare, and it''s the only thing that gives life real meaning.', 'Nicholas Sparks', 'LOVE'),
(gen_random_uuid(), 'Love is like the wind, you can''t see it but you can feel it.', 'Nicholas Sparks', 'LOVE'),
(gen_random_uuid(), 'The greatest thing you''ll ever learn is just to love and be loved in return.', 'Eden Ahbez', 'LOVE'),
(gen_random_uuid(), 'Love does not consist of gazing at each other, but in looking outward together in the same direction.', 'Antoine de Saint-Exupéry', 'LOVE'),
(gen_random_uuid(), 'To love and be loved is to feel the sun from both sides.', 'David Viscott', 'LOVE'),
(gen_random_uuid(), 'Love is an untamed force. When we try to control it, it destroys us. When we try to imprison it, it enslaves us.', 'Paulo Coelho', 'LOVE'),
(gen_random_uuid(), 'I saw that you were perfect, and so I loved you. Then I saw that you were not perfect and I loved you even more.', 'Angelita Lim', 'LOVE'),
(gen_random_uuid(), 'Love is when you meet someone who tells you something new about yourself.', 'André Breton', 'LOVE'),
(gen_random_uuid(), 'The giving of love is an education in itself.', 'Eleanor Roosevelt', 'LOVE'),
(gen_random_uuid(), 'Love is the only force capable of transforming an enemy into a friend.', 'Martin Luther King Jr.', 'LOVE');

-- ============================================================================
-- SUCCESS QUOTES (20+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'Success is not the key to happiness. Happiness is the key to success. If you love what you are doing, you will be successful.', 'Albert Schweitzer', 'SUCCESS'),
(gen_random_uuid(), 'The way to get started is to quit talking and begin doing.', 'Walt Disney', 'SUCCESS'),
(gen_random_uuid(), 'Don''t be afraid to give up the good to go for the great.', 'John D. Rockefeller', 'SUCCESS'),
(gen_random_uuid(), 'Innovation distinguishes between a leader and a follower.', 'Steve Jobs', 'SUCCESS'),
(gen_random_uuid(), 'The successful warrior is the average man, with laser-like focus.', 'Bruce Lee', 'SUCCESS'),
(gen_random_uuid(), 'Success usually comes to those who are too busy to be looking for it.', 'Henry David Thoreau', 'SUCCESS'),
(gen_random_uuid(), 'If you are not willing to risk the usual, you will have to settle for the ordinary.', 'Jim Rohn', 'SUCCESS'),
(gen_random_uuid(), 'The two most important days in your life are the day you are born and the day you find out why.', 'Mark Twain', 'SUCCESS'),
(gen_random_uuid(), 'I find that the harder I work, the more luck I seem to have.', 'Thomas Jefferson', 'SUCCESS'),
(gen_random_uuid(), 'Success is walking from failure to failure with no loss of enthusiasm.', 'Winston Churchill', 'SUCCESS'),
(gen_random_uuid(), 'The real test is not whether you avoid this failure, because you won''t. It''s whether you let it harden or shame you into inaction, or whether you learn from it.', 'Barack Obama', 'SUCCESS'),
(gen_random_uuid(), 'The only place where success comes before work is in the dictionary.', 'Vidal Sassoon', 'SUCCESS'),
(gen_random_uuid(), 'Try not to become a person of success, but rather try to become a person of value.', 'Albert Einstein', 'SUCCESS'),
(gen_random_uuid(), 'Don''t be distracted by criticism. Remember—the only taste of success some people get is to take a bite out of you.', 'Zig Ziglar', 'SUCCESS'),
(gen_random_uuid(), 'Success is not in what you have, but who you are.', 'Bo Bennett', 'SUCCESS'),
(gen_random_uuid(), 'The way to achieve your own success is to be willing to help somebody else get it first.', 'Iyanla Vanzant', 'SUCCESS'),
(gen_random_uuid(), 'Success is not just about what you accomplish in your life, it''s about what you inspire others to do.', 'Unknown', 'SUCCESS'),
(gen_random_uuid(), 'The difference between a successful person and others is not a lack of strength, not a lack of knowledge, but rather a lack of will.', 'Vince Lombardi', 'SUCCESS'),
(gen_random_uuid(), 'Success is the sum of small efforts repeated day in and day out.', 'Robert Collier', 'SUCCESS'),
(gen_random_uuid(), 'Success is not final, failure is not fatal: it is the courage to continue that counts.', 'Winston Churchill', 'SUCCESS'),
(gen_random_uuid(), 'The successful man is the one who finds out what is the matter with his business before his competitors do.', 'Roy L. Smith', 'SUCCESS'),
(gen_random_uuid(), 'There are no secrets to success. It is the result of preparation, hard work, and learning from failure.', 'Colin Powell', 'SUCCESS');

-- ============================================================================
-- WISDOM QUOTES (20+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'The only true wisdom is in knowing you know nothing.', 'Socrates', 'WISDOM'),
(gen_random_uuid(), 'Life is what happens to you while you''re busy making other plans.', 'John Lennon', 'WISDOM'),
(gen_random_uuid(), 'In three words I can sum up everything I''ve learned about life: it goes on.', 'Robert Frost', 'WISDOM'),
(gen_random_uuid(), 'The journey of a thousand miles begins with one step.', 'Lao Tzu', 'WISDOM'),
(gen_random_uuid(), 'To live is the rarest thing in the world. Most people just exist.', 'Oscar Wilde', 'WISDOM'),
(gen_random_uuid(), 'You must be the change you wish to see in the world.', 'Mahatma Gandhi', 'WISDOM'),
(gen_random_uuid(), 'It is better to be hated for what you are than to be loved for what you are not.', 'André Gide', 'WISDOM'),
(gen_random_uuid(), 'The unexamined life is not worth living.', 'Socrates', 'WISDOM'),
(gen_random_uuid(), 'We are what we repeatedly do. Excellence, then, is not an act, but a habit.', 'Aristotle', 'WISDOM'),
(gen_random_uuid(), 'The cave you fear to enter holds the treasure you seek.', 'Joseph Campbell', 'WISDOM'),
(gen_random_uuid(), 'The man who moves a mountain begins by carrying away small stones.', 'Confucius', 'WISDOM'),
(gen_random_uuid(), 'Knowing others is intelligence; knowing yourself is true wisdom. Mastering others is strength; mastering yourself is true power.', 'Lao Tzu', 'WISDOM'),
(gen_random_uuid(), 'The impediment to action advances action. What stands in the way becomes the way.', 'Marcus Aurelius', 'WISDOM'),
(gen_random_uuid(), 'Wisdom is not a product of schooling but of the lifelong attempt to acquire it.', 'Albert Einstein', 'WISDOM'),
(gen_random_uuid(), 'Yesterday I was clever, so I wanted to change the world. Today I am wise, so I am changing myself.', 'Rumi', 'WISDOM'),
(gen_random_uuid(), 'The wise find pleasure in water; the virtuous find pleasure in hills. The wise are active; the virtuous are tranquil. The wise are joyful; the virtuous live long.', 'Confucius', 'WISDOM'),
(gen_random_uuid(), 'A wise man can learn more from a foolish question than a fool can learn from a wise answer.', 'Bruce Lee', 'WISDOM'),
(gen_random_uuid(), 'Wisdom is the reward you get for a lifetime of listening when you''d have preferred to talk.', 'Doug Larson', 'WISDOM'),
(gen_random_uuid(), 'The fool doth think he is wise, but the wise man knows himself to be a fool.', 'William Shakespeare', 'WISDOM'),
(gen_random_uuid(), 'To gain knowledge, add things every day. To gain wisdom, subtract things every day.', 'Lao Tzu', 'WISDOM'),
(gen_random_uuid(), 'Wisdom is not a product of schooling but of the lifelong attempt to acquire it.', 'Albert Einstein', 'WISDOM'),
(gen_random_uuid(), 'The invariable mark of wisdom is to see the miraculous in the common.', 'Ralph Waldo Emerson', 'WISDOM');

-- ============================================================================
-- HUMOR QUOTES (20+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'I''m not superstitious, but I am a little stitious.', 'Michael Scott', 'HUMOR'),
(gen_random_uuid(), 'I am so clever that sometimes I don''t understand a single word of what I am saying.', 'Oscar Wilde', 'HUMOR'),
(gen_random_uuid(), 'The early bird might get the worm, but the second mouse gets the cheese.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I told my wife she was drawing her eyebrows too high. She looked surprised.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I''m not arguing, I''m just explaining why I''m right.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'The problem with quotes found on the internet is that they are often not true.', 'Abraham Lincoln', 'HUMOR'),
(gen_random_uuid(), 'I''m writing a book. I''ve got the page numbers done.', 'Steven Wright', 'HUMOR'),
(gen_random_uuid(), 'I don''t need anger management. I need people to stop annoying me.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I am not lazy, I am on energy-saving mode.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I asked God for a bike, but I know God doesn''t work that way. So I stole a bike and asked for forgiveness.', 'Emo Philips', 'HUMOR'),
(gen_random_uuid(), 'I told my computer I needed a break, and now it won''t stop sending me Kit-Kat ads.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'My therapist told me the way to achieve true inner peace is to finish what I start. So far I''ve finished two bags of M&Ms and a chocolate cake. I feel better already.', 'Dave Barry', 'HUMOR'),
(gen_random_uuid(), 'I''m not saying I''m Wonder Woman, I''m just saying no one has ever seen me and Wonder Woman in the same room together.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I used to think I was indecisive, but now I''m not so sure.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I''m not lazy, I''m just on my energy saving mode like your phone.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'Common sense is like deodorant. The people who need it most never use it.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I don''t have a problem with authority. I just have a problem with you being mine.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I''m not saying I''m Batman, I''m just saying no one has ever seen me and Batman in the same room together.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'My bed is a magical place where I suddenly remember everything I forgot to do.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I''m at a place in my life where errands are starting to count as going out.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I don''t always go the extra mile, but when I do, it''s because I missed my exit.', 'Unknown', 'HUMOR'),
(gen_random_uuid(), 'I''m not arguing, I''m just explaining why I''m right. There''s a difference.', 'Unknown', 'HUMOR');

-- ============================================================================
-- GENERAL QUOTES (Bonus - 10+)
-- ============================================================================

INSERT INTO public.quotes (id, text, author, category) VALUES
(gen_random_uuid(), 'Be yourself; everyone else is already taken.', 'Oscar Wilde', 'GENERAL'),
(gen_random_uuid(), 'Two things are infinite: the universe and human stupidity; and I''m not sure about the universe.', 'Albert Einstein', 'GENERAL'),
(gen_random_uuid(), 'So many books, so little time.', 'Frank Zappa', 'GENERAL'),
(gen_random_uuid(), 'A room without books is like a body without a soul.', 'Marcus Tullius Cicero', 'GENERAL'),
(gen_random_uuid(), 'Be the change that you wish to see in the world.', 'Mahatma Gandhi', 'GENERAL'),
(gen_random_uuid(), 'If you want to know what a man''s like, take a good look at how he treats his inferiors, not his equals.', 'J.K. Rowling', 'GENERAL'),
(gen_random_uuid(), 'Friendship is born at that moment when one person says to another: "What! You too? I thought I was the only one."', 'C.S. Lewis', 'GENERAL'),
(gen_random_uuid(), 'I''ve learned that people will forget what you said, people will forget what you did, but people will never forget how you made them feel.', 'Maya Angelou', 'GENERAL'),
(gen_random_uuid(), 'To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.', 'Ralph Waldo Emerson', 'GENERAL'),
(gen_random_uuid(), 'Insanity is doing the same thing, over and over again, but expecting different results.', 'Narcotics Anonymous', 'GENERAL'),
(gen_random_uuid(), 'It is our choices, Harry, that show what we truly are, far more than our abilities.', 'J.K. Rowling', 'GENERAL');

-- ============================================================================
-- Summary:
-- ============================================================================
-- Total quotes inserted: 110+
-- - Motivation: 22 quotes
-- - Love: 22 quotes
-- - Success: 22 quotes
-- - Wisdom: 22 quotes
-- - Humor: 22 quotes
-- - General: 11 quotes
-- ============================================================================
