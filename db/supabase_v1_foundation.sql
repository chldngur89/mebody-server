-- MEBODY backend v1 foundation migration
-- Run in Supabase SQL Editor. This file only extends existing structures where possible.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1) Extend existing user_profiles. Existing app uses id = auth.users.id.
CREATE TABLE IF NOT EXISTS public.user_profiles (
  id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email text,
  display_name text,
  marketing_opt_in boolean NOT NULL DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.user_profiles
  ADD COLUMN IF NOT EXISTS auth_user_id uuid UNIQUE,
  ADD COLUMN IF NOT EXISTS name text,
  ADD COLUMN IF NOT EXISTS nickname text,
  ADD COLUMN IF NOT EXISTS phone text,
  ADD COLUMN IF NOT EXISTS role text NOT NULL DEFAULT 'MEMBER',
  ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'ACTIVE',
  ADD COLUMN IF NOT EXISTS grade text NOT NULL DEFAULT 'BASIC',
  ADD COLUMN IF NOT EXISTS body_bti_code text,
  ADD COLUMN IF NOT EXISTS body_bti_title text,
  ADD COLUMN IF NOT EXISTS body_bti_description text,
  ADD COLUMN IF NOT EXISTS mission_achievement_rate numeric NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS deleted_at timestamptz;

UPDATE public.user_profiles
SET auth_user_id = id
WHERE auth_user_id IS NULL;

UPDATE public.user_profiles
SET name = COALESCE(name, display_name)
WHERE name IS NULL AND display_name IS NOT NULL;

UPDATE public.user_profiles
SET
  role = COALESCE(role, 'MEMBER'),
  status = COALESCE(status, 'ACTIVE'),
  grade = COALESCE(grade, 'BASIC'),
  mission_achievement_rate = COALESCE(mission_achievement_rate, 0);

ALTER TABLE public.user_profiles
  ALTER COLUMN role SET DEFAULT 'MEMBER',
  ALTER COLUMN status SET DEFAULT 'ACTIVE',
  ALTER COLUMN grade SET DEFAULT 'BASIC',
  ALTER COLUMN mission_achievement_rate SET DEFAULT 0;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'user_profiles_role_check') THEN
    ALTER TABLE public.user_profiles
      ADD CONSTRAINT user_profiles_role_check CHECK (role IN ('MEMBER', 'SELLER', 'ADMIN'));
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'user_profiles_status_check') THEN
    ALTER TABLE public.user_profiles
      ADD CONSTRAINT user_profiles_status_check CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'DELETED'));
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'user_profiles_grade_check') THEN
    ALTER TABLE public.user_profiles
      ADD CONSTRAINT user_profiles_grade_check CHECK (grade IN ('BASIC', 'BRONZE', 'SILVER', 'GOLD', 'VIP'));
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS user_profiles_role_idx ON public.user_profiles (role);
CREATE INDEX IF NOT EXISTS user_profiles_status_idx ON public.user_profiles (status);
CREATE INDEX IF NOT EXISTS user_profiles_grade_idx ON public.user_profiles (grade);
CREATE INDEX IF NOT EXISTS user_profiles_created_at_idx ON public.user_profiles (created_at DESC);
CREATE INDEX IF NOT EXISTS user_profiles_deleted_at_idx ON public.user_profiles (deleted_at);

DROP TRIGGER IF EXISTS user_profiles_updated_at ON public.user_profiles;
CREATE TRIGGER user_profiles_updated_at
BEFORE UPDATE ON public.user_profiles
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER AS $$
DECLARE
  resolved_name text;
BEGIN
  resolved_name := NULLIF(NEW.raw_user_meta_data ->> 'display_name', '');

  INSERT INTO public.user_profiles (
    id,
    auth_user_id,
    email,
    display_name,
    name,
    role,
    status,
    grade
  )
  VALUES (
    NEW.id,
    NEW.id,
    NEW.email,
    resolved_name,
    resolved_name,
    'MEMBER',
    'ACTIVE',
    'BASIC'
  )
  ON CONFLICT (id) DO UPDATE SET
    auth_user_id = COALESCE(public.user_profiles.auth_user_id, EXCLUDED.auth_user_id),
    email = COALESCE(public.user_profiles.email, EXCLUDED.email),
    display_name = COALESCE(public.user_profiles.display_name, EXCLUDED.display_name),
    name = COALESCE(public.user_profiles.name, EXCLUDED.name),
    updated_at = now();

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_auth_user();

INSERT INTO public.user_profiles (
  id,
  auth_user_id,
  email,
  display_name,
  name,
  role,
  status,
  grade
)
SELECT
  u.id,
  u.id,
  u.email,
  NULLIF(u.raw_user_meta_data ->> 'display_name', ''),
  NULLIF(u.raw_user_meta_data ->> 'display_name', ''),
  'MEMBER',
  'ACTIVE',
  'BASIC'
FROM auth.users u
WHERE NOT EXISTS (
  SELECT 1
  FROM public.user_profiles p
  WHERE p.id = u.id
);

-- 2) BodyBTI result history for dashboard and admin views.
CREATE TABLE IF NOT EXISTS public.body_bti_results (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid REFERENCES public.user_profiles(id) ON DELETE SET NULL,
  code text NOT NULL,
  title text,
  description text,
  score_json jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS body_bti_results_user_id_created_at_idx
  ON public.body_bti_results (user_id, created_at DESC);

-- 3) Mission catalog and progress.
CREATE TABLE IF NOT EXISTS public.missions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title text NOT NULL,
  description text,
  target_count integer NOT NULL DEFAULT 1,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.user_mission_progress (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid REFERENCES public.user_profiles(id) ON DELETE CASCADE,
  mission_id uuid REFERENCES public.missions(id) ON DELETE CASCADE,
  current_count integer NOT NULL DEFAULT 0,
  target_count integer NOT NULL DEFAULT 1,
  achievement_rate numeric NOT NULL DEFAULT 0,
  completed_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, mission_id)
);

CREATE INDEX IF NOT EXISTS user_mission_progress_user_id_idx
  ON public.user_mission_progress (user_id);

DROP TRIGGER IF EXISTS user_mission_progress_updated_at ON public.user_mission_progress;
CREATE TRIGGER user_mission_progress_updated_at
BEFORE UPDATE ON public.user_mission_progress
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- 4) Product shell for store/seller pages.
CREATE TABLE IF NOT EXISTS public.products (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id uuid REFERENCES public.user_profiles(id) ON DELETE SET NULL,
  name text NOT NULL,
  description text,
  price numeric,
  image_url text,
  status text NOT NULL DEFAULT 'DRAFT',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_status_check') THEN
    ALTER TABLE public.products
      ADD CONSTRAINT products_status_check CHECK (status IN ('DRAFT', 'ACTIVE', 'SOLD_OUT', 'ARCHIVED'));
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS products_status_idx ON public.products (status);
CREATE INDEX IF NOT EXISTS products_seller_id_idx ON public.products (seller_id);

DROP TRIGGER IF EXISTS products_updated_at ON public.products;
CREATE TRIGGER products_updated_at
BEFORE UPDATE ON public.products
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- 5) Admin audit logs.
CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_user_id uuid REFERENCES public.user_profiles(id) ON DELETE SET NULL,
  action text NOT NULL,
  target_type text,
  target_id uuid,
  before_json jsonb,
  after_json jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS admin_audit_logs_admin_user_id_idx
  ON public.admin_audit_logs (admin_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS admin_audit_logs_target_idx
  ON public.admin_audit_logs (target_type, target_id, created_at DESC);

-- Existing questionnaire result link remains auth.users.id based.
ALTER TABLE public.questionnaire_responses
  ADD COLUMN IF NOT EXISTS user_id uuid REFERENCES auth.users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS questionnaire_responses_user_id_idx
  ON public.questionnaire_responses (user_id, completed_at DESC);

-- Keep RLS safe for client reads/writes already used by the SPA.
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.missions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_mission_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.body_bti_results ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'products' AND policyname = 'products public read active') THEN
    CREATE POLICY "products public read active"
      ON public.products FOR SELECT
      USING (status = 'ACTIVE');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'user_profiles' AND policyname = 'user_profiles select own') THEN
    CREATE POLICY "user_profiles select own"
      ON public.user_profiles FOR SELECT
      USING (auth.uid() = id OR auth.uid() = auth_user_id);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'user_profiles' AND policyname = 'user_profiles insert own') THEN
    CREATE POLICY "user_profiles insert own"
      ON public.user_profiles FOR INSERT
      WITH CHECK (auth.uid() = id OR auth.uid() = auth_user_id);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'user_profiles' AND policyname = 'user_profiles update own') THEN
    CREATE POLICY "user_profiles update own"
      ON public.user_profiles FOR UPDATE
      USING (auth.uid() = id OR auth.uid() = auth_user_id)
      WITH CHECK (auth.uid() = id OR auth.uid() = auth_user_id);
  END IF;
END $$;

-- Seed placeholder store products only if empty.
INSERT INTO public.products (name, description, price, image_url, status)
SELECT * FROM (VALUES
  ('MEBODY 폼롤러', '목·어깨·골반 루틴 전후에 쓰기 좋은 기본 회복 도구입니다.', 29000, null, 'ACTIVE'),
  ('밸런스 스트레칭 밴드', '15분 케어 루틴에서 움직임 범위를 확인하기 좋은 밴드입니다.', 19000, null, 'ACTIVE'),
  ('마사지볼 듀오', '작은 부위 이완과 셀프 케어에 활용할 수 있는 마사지볼 세트입니다.', 15000, null, 'ACTIVE')
) AS seed(name, description, price, image_url, status)
WHERE NOT EXISTS (SELECT 1 FROM public.products);

COMMIT;
