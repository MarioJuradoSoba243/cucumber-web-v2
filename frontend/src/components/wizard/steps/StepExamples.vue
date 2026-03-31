<script setup lang="ts">
import type { FeatureDocument } from '../../../types/feature'
defineProps<{ feature: FeatureDocument }>()
</script>
<template>
  <div>
    <div v-for="scenario in feature.scenarios.filter((s) => s.type === 'OUTLINE')" :key="scenario.id" class="card">
      <h4>{{ scenario.name }}</h4>
      <p class="muted">Tables: {{ scenario.exampleTables.length }}</p>
    </div>
    <p v-if="feature.scenarios.filter((s) => s.type === 'OUTLINE').length === 0" class="empty">No hay outlines.</p>
  </div>
</template>
