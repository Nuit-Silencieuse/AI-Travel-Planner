import { createClient } from '@supabase/supabase-js'

// ===================================================================
// = IMPORTANT: Replace the placeholder values below with your      =
// = actual Supabase project details.                               =
// ===================================================================

// Find these in your Supabase project settings under: API
const supabaseUrl = 'https://ocgqfsevvmxxcnnlwwyn.supabase.co'
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jZ3Fmc2V2dm14eGNubmx3d3luIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI3NDY4NDYsImV4cCI6MjA3ODMyMjg0Nn0.Ua-I3UVokC2Nh63uGi8DadroYHkxatafwcW846OQebY'

export const supabase = createClient(supabaseUrl, supabaseKey)
