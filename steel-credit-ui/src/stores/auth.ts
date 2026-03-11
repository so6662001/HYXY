import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const enterpriseId = ref<number>(0)
  const enterpriseName = ref<string>('')
  const token = ref<string>('')

  function setAuth(id: number, name: string, authToken: string) {
    enterpriseId.value = id
    enterpriseName.value = name
    token.value = authToken
  }

  function clear() {
    enterpriseId.value = 0
    enterpriseName.value = ''
    token.value = ''
  }

  return { enterpriseId, enterpriseName, token, setAuth, clear }
})
