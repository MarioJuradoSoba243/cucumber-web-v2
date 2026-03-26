<script setup lang="ts">
import type { Scenario } from '../types/feature'
import ExamplesTableEditor from './ExamplesTableEditor.vue'

const props = defineProps<{ scenario: Scenario }>()

function addStep() {
  props.scenario.steps.push({ keyword: 'AND', text: '' })
}

function addExamples() {
  props.scenario.exampleTables.push({ id: crypto.randomUUID(), name: '', tags: [], columns: ['param'], rows: [] })
}
</script>

<template>
  <article class="scenario">
    <header>
      <input v-model="scenario.name" class="scenario-title" />
      <span class="badge">{{ scenario.type }}</span>
    </header>

    <div class="steps">
      <div v-for="(step, index) in scenario.steps" :key="index" class="step-row">
        <select v-model="step.keyword">
          <option>GIVEN</option><option>WHEN</option><option>THEN</option><option>AND</option><option>BUT</option>
        </select>
        <input v-model="step.text" placeholder="Step..." />
      </div>
      <button @click="addStep">+ Step</button>
    </div>

    <div v-if="scenario.type === 'OUTLINE'" class="examples-section">
      <h4>Examples</h4>
      <ExamplesTableEditor v-for="table in scenario.exampleTables" :key="table.id" :table="table" />
      <button @click="addExamples">+ Bloque Examples</button>
    </div>
  </article>
</template>
