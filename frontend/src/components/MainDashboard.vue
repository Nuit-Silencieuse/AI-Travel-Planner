<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { supabase } from '@/supabase'

const authStore = useAuthStore()

// Form state
const destination = ref('')
const startDate = ref('')
const endDate = ref('')
const budget = ref('')
const preferences = ref('')

// UI state
const generatedPlan = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
const isListening = ref(false)

// Map state
const map = ref(null)
let recognition = null
let markers = [];
let infoWindow = null; // Reusable InfoWindow

// --- Map Logic ---
const initMap = () => {
  if (window.AMap) {
    map.value = new AMap.Map('map-container', {
      zoom: 11,
      center: [116.397428, 39.90923], // Default center (Beijing)
      viewMode: '3D'
    });
    // Initialize the InfoWindow
    infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30)
    });
  } else {
    console.error('AMap script not loaded');
  }
}

watch(generatedPlan, (newPlan) => {
  if (!newPlan || !map.value) return;

  map.value.remove(markers);
  markers = [];

  const newMarkers = [];
  newPlan.days.forEach(day => {
    day.activities.forEach(activity => {
      if (activity.location && activity.location.lng && activity.location.lat) {
        const position = new AMap.LngLat(activity.location.lng, activity.location.lat);
        
        const marker = new AMap.Marker({
          position: position,
          map: map.value,
          title: activity.description
        });

        // --- Add Click Listener ---
        marker.on('click', () => {
          const content = `
            <div class="info-window-content">
              <strong>${activity.time}</strong>: ${activity.description}
            </div>
          `;
          infoWindow.setContent(content);
          infoWindow.open(map.value, marker.getPosition());
        });

        newMarkers.push(marker);
      }
    });
  });

  if (newMarkers.length > 0) {
    markers = newMarkers;
    map.value.setFitView(); // Adjust map to fit all markers
  }
});

onMounted(() => {
  initMap();
});


// --- API & Form Logic ---
const handleLogout = async () => {
  try {
    await authStore.signOut()
  } catch (error) {
    alert(error.message)
  }
}

const handleGeneratePlan = async () => {
  isLoading.value = true
  errorMessage.value = ''
  generatedPlan.value = null

  try {
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) throw new Error('No active session found. Please log in again.')

    const response = await fetch('http://localhost:8080/api/plans/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${session.access_token}`
      },
      body: JSON.stringify({
        destination: destination.value,
        startDate: startDate.value || null,
        endDate: endDate.value || null,
        budget: budget.value || null,
        preferences: preferences.value || null
      })
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || 'Failed to generate travel plan.')
    }

    const data = await response.json()
    generatedPlan.value = data.planDetails ? JSON.parse(data.planDetails) : data;
  } catch (error) {
    console.error('Error generating plan:', error)
    errorMessage.value = error.message || 'An unexpected error occurred.'
  } finally {
    isLoading.value = false
  }
}

// --- Speech Recognition Logic ---
const toggleListen = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    alert('Your browser does not support the Web Speech API. Please try Chrome or Edge.')
    return
  }
  if (isListening.value) {
    recognition.stop()
    return
  }
  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.interimResults = false
  recognition.continuous = false
  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript
    preferences.value = preferences.value ? `${preferences.value} ${transcript}` : transcript
  }
  recognition.onerror = (event) => {
    console.error('Speech recognition error:', event.error)
    alert(`Speech recognition error: ${event.error}`)
  }
  recognition.onend = () => { isListening.value = false }
  recognition.start()
  isListening.value = true
}
</script>

<template>
  <div class="dashboard-layout">
    <div class="sidebar">
      <div class="header-bar">
        <span>{{ authStore.user?.email }}</span>
        <button @click="handleLogout" class="logout-button">Logout</button>
      </div>

      <div class="sidebar-content">
        <h2>Create Your Adventure</h2>
        <form @submit.prevent="handleGeneratePlan" class="plan-form">
          <!-- Form fields -->
          <div class="form-group">
            <label for="destination">Destination</label>
            <input type="text" id="destination" v-model="destination" placeholder="e.g., Paris, France" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="start-date">Start Date</label>
              <input type="date" id="start-date" v-model="startDate" />
            </div>
            <div class="form-group">
              <label for="end-date">End Date</label>
              <input type="date" id="end-date" v-model="endDate" />
            </div>
          </div>
          <div class="form-group">
            <label for="budget">Budget (Optional)</label>
            <input type="number" id="budget" v-model="budget" placeholder="e.g., 1000" />
          </div>
          <div class="form-group">
            <div class="label-with-mic">
              <label for="preferences">Preferences (Optional)</label>
              <button type="button" @click="toggleListen" class="mic-button" :class="{ 'is-listening': isListening }">
                {{ isListening ? 'Listening...' : '🎤' }}
              </button>
            </div>
            <textarea id="preferences" v-model="preferences" placeholder="e.g., love museums, prefer local food..."></textarea>
          </div>
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? 'Generating...' : 'Generate Plan' }}
          </button>
        </form>

        <!-- Results display -->
        <div v-if="isLoading" class="loading-spinner">AI is thinking...</div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <div v-if="generatedPlan" class="plan-display">
          <h3>Your Itinerary</h3>
          <div v-for="dayPlan in generatedPlan.days" :key="dayPlan.day" class="day-plan">
            <h4>Day {{ dayPlan.day }}</h4>
            <ul>
              <li v-for="(activity, index) in dayPlan.activities" :key="index">
                <strong>{{ activity.time }}</strong>: {{ activity.description }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div id="map-container" class="map-container"></div>
  </div>
</template>

<style>
/* Info window styles need to be global as they are injected by AMap */
.info-window-content {
  padding: 5px;
  font-size: 14px;
}
</style>

<style scoped>
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
}
.sidebar {
  width: 400px;
  flex-shrink: 0;
  background: #fdfdfd;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 5px rgba(0,0,0,0.1);
  z-index: 10;
}
.sidebar-content {
  padding: 20px;
  overflow-y: auto;
}
.map-container {
  flex-grow: 1;
}
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}
.logout-button {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
}
h2 {
  text-align: center;
  margin-top: 0;
  margin-bottom: 20px;
}
.plan-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.form-row {
  display: flex;
  gap: 15px;
}
.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}
label {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 0.9rem;
}
.label-with-mic {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mic-button {
  background: #f0f0f0;
  border: 1px solid #ccc;
  color: #333;
  padding: 5px 10px;
  font-size: 0.9rem;
  border-radius: 4px;
}
.mic-button.is-listening {
  background: #ff4136;
  color: white;
}
input, textarea {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}
textarea {
  resize: vertical;
  min-height: 60px;
}
button[type="submit"] {
  padding: 12px;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
}
button:disabled {
  background-color: #ccc;
}
.loading-spinner, .error-message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 4px;
  text-align: center;
}
.error-message {
  background-color: #fdd;
  color: #c00;
}
.plan-display {
  margin-top: 20px;
}
.day-plan {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  background-color: #f9f9f9;
}
.day-plan h4 {
  margin-top: 0;
  color: #007bff;
}
.day-plan ul {
  list-style: none;
  padding: 0;
}
.day-plan li {
  margin-bottom: 5px;
  font-size: 0.95rem;
}
</style>
