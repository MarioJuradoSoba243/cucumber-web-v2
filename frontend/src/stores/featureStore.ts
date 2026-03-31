import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../services/api'
import type { DirectoryNode, FeatureDocument, FeatureSummary } from '../types/feature'

export const useFeatureStore = defineStore('feature', () => {
  const features = ref<FeatureSummary[]>([])
  const selectedFeature = ref<FeatureDocument | null>(null)
  const loading = ref(false)
  const error = ref('')
  const query = ref('')
  const message = ref('')
  const tree = ref<DirectoryNode | null>(null)

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
      console.info('[featureStore] Loading features', query.value)
      features.value = await api.listFeatures(query.value)
      tree.value = await api.getFoldersTree()
    } catch (err: any) {
      error.value = err.message ?? 'Error loading features'
    } finally {
      loading.value = false
    }
  }

  async function openFeature(id: string) {
    loading.value = true
    try {
      console.info('[featureStore] Opening feature', id)
      selectedFeature.value = await api.getFeature(id)
    } finally {
      loading.value = false
    }
  }

  async function saveFeature() {
    if (!selectedFeature.value) return
    console.info('[featureStore] Saving feature', selectedFeature.value.id)
    selectedFeature.value = await api.updateFeature(selectedFeature.value)
    message.value = 'Feature guardada correctamente'
    await loadFeatures()
  }

  async function createFeature(name: string, folderPath = '') {
    const cleanName = name.trim() || 'Nueva Feature'
    const fileName = cleanName.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9\-]/g, '')
    const normalizedFolder = folderPath.trim().replace(/^\/+|\/+$/g, '')
    const featureId = normalizedFolder ? `${normalizedFolder}/${fileName || 'feature'}.feature` : `${fileName || 'feature'}.feature`
    const draft: FeatureDocument = {
      id: featureId,
      filePath: '',
      name: cleanName,
      description: '',
      tags: [],
      scenarios: [],
      validation: { errors: [], warnings: [], missingColumns: [], unusedColumns: [], emptyCells: [] }
    }
    console.info('[featureStore] Creating feature', draft.id)
    selectedFeature.value = await api.createFeature(draft)
    message.value = 'Feature creada correctamente'
    await loadFeatures()
  }

  async function createFolder(parentPath: string, name: string) {
    tree.value = await api.createFolder(parentPath, name)
    message.value = 'Carpeta creada correctamente'
  }

  async function renamePath(path: string, newName: string) {
    tree.value = await api.renamePath(path, newName)
    message.value = 'Nombre actualizado correctamente'
    await loadFeatures()
  }

  async function movePath(sourcePath: string, destinationFolderPath: string) {
    tree.value = await api.movePath(sourcePath, destinationFolderPath)
    message.value = 'Elemento movido correctamente'
    await loadFeatures()
  }

  return {
    features, tree, selectedFeature, loading, error, query, metrics, message,
    loadFeatures, openFeature, saveFeature, createFeature, createFolder, renamePath, movePath
  }
})
