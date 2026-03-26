import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../services/api'
import type { FeatureDocument, FeatureSummary } from '../types/feature'

export const useFeatureStore = defineStore('feature', () => {
  const features = ref<FeatureSummary[]>([])
  const selectedFeature = ref<FeatureDocument | null>(null)
  const loading = ref(false)
  const error = ref('')
  const query = ref('')

  const metrics = computed(() => {
    const outlines = features.value.reduce((acc, it) => acc + it.outlineCount, 0)
    const scenarios = features.value.reduce((acc, it) => acc + it.scenarioCount, 0)
    const rows = features.value.reduce((acc, it) => acc + it.totalExampleRows, 0)
    return {
      features: features.value.length,
      scenarios,
      outlines,
      examples: rows
    }
  })

  async function loadFeatures() {
    loading.value = true
    error.value = ''
    try {
      features.value = await api.listFeatures(query.value)
    } catch (err: any) {
      error.value = err.message ?? 'Error loading features'
    } finally {
      loading.value = false
    }
  }

  async function openFeature(id: string) {
    loading.value = true
    try {
      selectedFeature.value = await api.getFeature(id)
    } finally {
      loading.value = false
    }
  }

  async function saveFeature() {
    if (!selectedFeature.value) return
    selectedFeature.value = await api.updateFeature(selectedFeature.value)
    await loadFeatures()
  }

  return { features, selectedFeature, loading, error, query, metrics, loadFeatures, openFeature, saveFeature }
})
