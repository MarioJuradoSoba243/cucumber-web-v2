<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Scenario } from '../types/feature'
import ExamplesTableEditor from './ExamplesTableEditor.vue'

const props = defineProps<{ scenario: Scenario }>()
const collapsed = ref(false)

const placeholderColumns = computed(() => {
  const placeholders = new Set<string>()

  props.scenario.steps.forEach((step) => {
    for (const match of step.text.matchAll(/<([^>]+)>/g)) {
      if (match[1]) {
        placeholders.add(match[1])
      }
    }
  })

  return Array.from(placeholders)
})

function addStep() {
  props.scenario.steps.push({ keyword: 'AND', text: '' })
}

function removeStep(index: number) {
  props.scenario.steps.splice(index, 1)
}

function addExamples() {
  props.scenario.exampleTables.push({ id: crypto.randomUUID(), name: '', tags: [], columns: ['param'], rows: [] })
}
</script>

<template>
  <article class="scenario card">
    <header class="scenario-header">
      <div>
        <input v-model="scenario.name" class="scenario-title" :placeholder="scenario.type === 'OUTLINE' ? 'Nuevo Scenario Outline' : 'Nuevo Scenario'" />
        <p class="muted">{{ scenario.steps.length }} pasos</p>
      </div>
      <div class="scenario-header-actions">
        <button
          class="ghost collapse-btn"
          type="button"
          :aria-expanded="!collapsed"
          @click="collapsed = !collapsed"
        >
          {{ collapsed ? '▸ Expandir' : '▾ Colapsar' }}
        </button>
        <span class="badge" :class="scenario.type === 'OUTLINE' ? 'warning' : 'info'">{{ scenario.type }}</span>
      </div>
    </header>

    <div v-show="!collapsed" class="scenario-content">
      <div class="steps">
      <div v-for="(step, index) in scenario.steps" :key="index" class="step-row">
        <select v-model="step.keyword">
          <option>GIVEN</option>
          <option>WHEN</option>
          <option>THEN</option>
          <option>AND</option>
          <option>BUT</option>
        </select>
        <input v-model="step.text" placeholder="Describe el paso... (usa <param> para outlines)" />
        <button class="ghost icon-btn" title="Eliminar paso" @click="removeStep(index)">🗑</button>
      </div>
      <button class="secondary" @click="addStep">+ Añadir paso</button>
      </div>

      <div v-if="scenario.type === 'OUTLINE'" class="examples-section">
        <div class="examples-header">
          <h4>Examples</h4>
          <span class="badge neutral">Placeholders: {{ placeholderColumns.length }}</span>
        </div>
        <ExamplesTableEditor
          v-for="table in scenario.exampleTables"
          :key="table.id"
          :table="table"
          :placeholder-columns="placeholderColumns"
        />
        <button class="secondary" @click="addExamples">+ Bloque Examples</button>
      </div>
    </div>
  </article>
</template>
