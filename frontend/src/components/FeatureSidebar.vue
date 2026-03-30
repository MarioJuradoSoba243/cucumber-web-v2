<script setup lang="ts">
import { ref } from 'vue'
import type { FeatureSummary } from '../types/feature'

defineProps<{
  items: FeatureSummary[]
  selectedId?: string
  loading: boolean
  query: string
  collapsed: boolean
}>()

const emit = defineEmits<{
  select: [id: string]
  search: [value: string]
  create: [name: string]
  toggle: []
}>()

const featureName = ref('')


function createFeature() {
  emit('create', featureName.value)
  featureName.value = ''
}
</script>

<template>
  <aside :class="['sidebar', { collapsed }]">
    <div class="sidebar-header">
      <button class="ghost icon-btn" @click="emit('toggle')">{{ collapsed ? '☰' : '⟨' }}</button>
      <div v-if="!collapsed" class="brand">
        <h2>Cucumber Studio</h2>
        <span>QA Workspace</span>
      </div>
    </div>

    <template v-if="!collapsed">
      <label class="search-wrap">
        <span>🔎</span>
        <input :value="query" placeholder="Buscar features" @input="emit('search', ($event.target as HTMLInputElement).value)" />
      </label>

      <div class="feature-list-header">
        <h3>Features</h3>
        <span class="badge neutral">{{ items.length }}</span>
      </div>

      <div v-if="loading" class="sidebar-state">Cargando features...</div>
      <div v-else-if="!items.length" class="sidebar-state">No hay features con ese filtro.</div>

      <div class="feature-list">
        <button
          v-for="feature in items"
          :key="feature.id"
          :class="['feature-item', { active: selectedId === feature.id }]"
          @click="emit('select', feature.id)"
        >
          <div class="feature-item-title">{{ feature.name }}</div>
          <div class="meta">{{ feature.scenarioCount }} escenarios · {{ feature.totalExampleRows }} rows</div>
        </button>
      </div>

      <div class="sidebar-footer">
        <input v-model="featureName" placeholder="Nueva feature" @keyup.enter="createFeature" />
        <button class="primary" @click="createFeature">+ Nueva feature</button>
      </div>
    </template>
  </aside>
</template>
