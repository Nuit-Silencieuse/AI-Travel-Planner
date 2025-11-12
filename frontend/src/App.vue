<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import MainDashboard from './components/MainDashboard.vue'

const authStore = useAuthStore()

const email = ref('')
const password = ref('')

onMounted(() => {
  authStore.initialize()
})

const handleLogin = async () => {
  try {
    await authStore.signIn(email.value, password.value)
  } catch (error) {
    alert(error.message)
  }
}

const handleSignup = async () => {
  try {
    await authStore.signUp(email.value, password.value)
  } catch (error) {
    alert(error.message)
  }
}
</script>

<template>
  <div id="app-container">
    <div v-if="authStore.user">
      <MainDashboard />
    </div>
    <div v-else class="login-container">
      <h1>AI Travel Planner</h1>
      <form @submit.prevent>
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" v-model="email" placeholder="Your email" />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" placeholder="Your password" />
        </div>
        <div class="button-group">
          <button @click="handleLogin">Sign In</button>
          <button @click="handleSignup">Sign Up</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
#app-container {
  font-family: sans-serif;
  height: 100vh;
  width: 100vw;
}

/* This is the fix: ensure the wrapper for the dashboard also has full height */
#app-container > div {
  height: 100%;
  width: 100%;
}

.login-container {
  max-width: 500px;
  height: auto; /* Override the full height for the login container */
  margin: 40px auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

h1 {
  text-align: center;
  color: #333;
}
/* ... (rest of the styles for login form remain the same) ... */
.form-group {
  margin-bottom: 15px;
  width: 100%;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.button-group {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 15px;
}

button {
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
  flex-grow: 1;
}

button:first-child {
  margin-right: 10px;
  background-color: #28a745;
}
</style>
