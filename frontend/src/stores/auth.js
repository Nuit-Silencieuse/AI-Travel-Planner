import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { supabase } from '@/supabase'

export const useAuthStore = defineStore('auth', () => {
  const session = ref(null)
  const user = computed(() => session.value?.user ?? null)

  /**
   * Initialize the session from Supabase
   */
  const initialize = async () => {
    const { data } = await supabase.auth.getSession()
    session.value = data.session
  }

  /**
   * Sign in a user
   * @param {string} email
   * @param {string} password
   */
  const signIn = async (email, password) => {
    const { data, error } = await supabase.auth.signInWithPassword({
      email,
      password,
    })
    if (error) throw error
    session.value = data.session
  }

  /**
   * Sign up a new user
   * @param {string} email
   * @param {string} password
   */
  const signUp = async (email, password) => {
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
    })
    if (error) throw error
    session.value = data.session
  }

  /**
   * Sign out the current user
   */
  const signOut = async () => {
    const { error } = await supabase.auth.signOut()
    if (error) throw error
    session.value = null
  }

  // Listen for changes in auth state
  supabase.auth.onAuthStateChange((event, newSession) => {
    session.value = newSession
  })

  return {
    session,
    user,
    initialize,
    signIn,
    signUp,
    signOut,
  }
})
