<script setup lang="ts">
import type { FeatureDocument } from '../../../types/feature'
const props = defineProps<{ feature: FeatureDocument }>()
function addScenario(type: 'SCENARIO' | 'OUTLINE') {
  props.feature.scenarios.push({
    id: crypto.randomUUID(),
    type,
    name: type === 'OUTLINE' ? 'Nuevo Outline' : 'Nuevo Scenario',
    tags: [],
    steps: [],
    exampleTables: type === 'OUTLINE' ? [{ id: crypto.randomUUID(), name: 'Default', tags: [], columns: ['param'], rows: [] }] : []
  })
}

function removeScenario(id: string) {
  const index = props.feature.scenarios.findIndex((it) => it.id === id)
  if (index >= 0) {
    props.feature.scenarios.splice(index, 1)
  }
}
</script>
<template>
  <div>
    <button class="secondary" @click="addScenario('SCENARIO')">+ Scenario</button>
    <button class="secondary" @click="addScenario('OUTLINE')">+ Outline</button>
    <ul>
      <li v-for="scenario in feature.scenarios" :key="scenario.id">
        <input v-model="scenario.name" />
        <button class="ghost" @click="removeScenario(scenario.id)">Eliminar</button>
      </li>
    </ul>
  </div>
</template>
