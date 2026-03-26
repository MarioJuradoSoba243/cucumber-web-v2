<script setup lang="ts">
import type { FeatureSummary } from '../types/feature'

defineProps<{ items: FeatureSummary[]; selectedId?: string; loading: boolean }>()
const emit = defineEmits<{ select: [id: string] }>()
</script>

<template>
  <aside class="sidebar">
    <h2>Features</h2>
    <div v-if="loading" class="muted">Cargando...</div>
    <button
      v-for="feature in items"
      :key="feature.id"
      :class="['feature-item', { active: selectedId === feature.id }]"
      @click="emit('select', feature.id)"
    >
      <div class="title">{{ feature.name }}</div>
      <div class="meta">{{ feature.scenarioCount }} escenarios · {{ feature.totalExampleRows }} rows</div>
    </button>
  </aside>
</template>
