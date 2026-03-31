<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { api } from '../services/api'
import type { ExportSelectionRequest, FeatureDocument } from '../types/feature'

const props = defineProps<{ feature: FeatureDocument }>()

const emit = defineEmits<{ (event: 'done', message: string): void }>()

type DestinationMode = 'browser' | 'choose-folder'

const filename = ref('')
const destinationMode = ref<DestinationMode>('browser')
const includeBackground = ref(true)
const includeDescription = ref(true)
const includeTags = ref(true)
const saving = ref(false)

const scenarioSelection = ref<Record<string, boolean>>({})
const rowSelection = ref<Record<string, Record<string, boolean>>>({})

const sortedScenarios = computed(() => [...props.feature.scenarios].sort((a, b) => a.name.localeCompare(b.name)))

const selectedScenariosCount = computed(() => Object.values(scenarioSelection.value).filter(Boolean).length)
const selectedRowsCount = computed(() =>
  Object.values(rowSelection.value)
    .flatMap((tableSelection) => Object.values(tableSelection))
    .filter(Boolean).length
)

const canExport = computed(() => selectedScenariosCount.value > 0)

function ensureFilenameDefaults() {
  if (filename.value.trim()) {
    return
  }
  const base = props.feature.name?.trim() || 'feature'
  filename.value = `${base.replace(/\s+/g, '-').toLowerCase()}.feature`
}

function ensureSelectionDefaults() {
  for (const scenario of props.feature.scenarios) {
    if (!(scenario.id in scenarioSelection.value)) {
      scenarioSelection.value[scenario.id] = true
    }

    for (const table of scenario.exampleTables) {
      if (!rowSelection.value[table.id]) {
        rowSelection.value[table.id] = {}
      }
      for (const row of table.rows) {
        if (!(row.id in rowSelection.value[table.id])) {
          rowSelection.value[table.id][row.id] = true
        }
      }
    }
  }
}

ensureFilenameDefaults()
ensureSelectionDefaults()

watch(
  () => props.feature,
  () => {
    ensureFilenameDefaults()
    ensureSelectionDefaults()
  },
  { deep: true }
)

function toggleAllScenarios(value: boolean) {
  for (const scenario of props.feature.scenarios) {
    scenarioSelection.value[scenario.id] = value
  }
}

function toggleAllRowsForTable(tableId: string, value: boolean) {
  const tableSelection = rowSelection.value[tableId]
  if (!tableSelection) return
  for (const rowId of Object.keys(tableSelection)) {
    tableSelection[rowId] = value
  }
}

function selectedRowsForTable(tableId: string) {
  const tableSelection = rowSelection.value[tableId] || {}
  return Object.values(tableSelection).filter(Boolean).length
}

function buildPayload(): ExportSelectionRequest {
  const featureClone: FeatureDocument = JSON.parse(JSON.stringify(props.feature))
  if (!includeBackground.value) {
    delete featureClone.background
  }
  if (!includeDescription.value) {
    featureClone.description = ''
  }
  if (!includeTags.value) {
    featureClone.tags = []
    featureClone.scenarios.forEach((scenario) => {
      scenario.tags = []
      scenario.exampleTables.forEach((table) => {
        table.tags = []
      })
    })
  }

  const scenarios = featureClone.scenarios.map((scenario) => ({
    scenarioId: scenario.id,
    selected: scenarioSelection.value[scenario.id] ?? false,
    exampleRowIdsByTableId: Object.fromEntries(
      scenario.exampleTables.map((table) => [
        table.id,
        table.rows
          .map((row) => row.id)
          .filter((rowId) => rowSelection.value[table.id]?.[rowId] ?? false)
      ])
    )
  }))

  return { feature: featureClone, scenarios }
}

async function saveToFile(content: string) {
  const sanitizedName = filename.value.trim().endsWith('.feature') ? filename.value.trim() : `${filename.value.trim()}.feature`

  const savePickerWindow = window as Window & {
    showSaveFilePicker?: (options: {
      suggestedName: string
      types: { description: string; accept: Record<string, string[]> }[]
    }) => Promise<{ createWritable: () => Promise<{ write: (value: string) => Promise<void>; close: () => Promise<void> }> }>
  }

  if (destinationMode.value === 'choose-folder' && savePickerWindow.showSaveFilePicker) {
    const handle = await savePickerWindow.showSaveFilePicker({
      suggestedName: sanitizedName,
      types: [{ description: 'Cucumber Feature', accept: { 'text/plain': ['.feature'] } }]
    })
    const writable = await handle.createWritable()
    await writable.write(content)
    await writable.close()
    emit('done', `Exportación completada en ubicación elegida: ${sanitizedName}`)
    return
  }

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const link = document.createElement('a')
  const href = URL.createObjectURL(blob)
  link.href = href
  link.download = sanitizedName
  link.click()
  URL.revokeObjectURL(href)
  emit('done', `Exportación descargada: ${sanitizedName}`)
}

async function exportSelection() {
  if (!canExport.value || saving.value) {
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    const response = await api.exportSelection(payload)
    await saveToFile(response)
  } finally {
    saving.value = false
  }
}

function selectOnlyOutlines() {
  for (const scenario of props.feature.scenarios) {
    scenarioSelection.value[scenario.id] = scenario.type === 'OUTLINE'
  }
}
</script>

<template>
  <section class="tab-content fade-in export-layout">
    <div class="card section-card export-card">
      <h3>Configuración de exportación</h3>
      <div class="export-grid">
        <div>
          <label>Nombre del fichero</label>
          <input v-model="filename" placeholder="mi-feature.feature" />
        </div>
        <div>
          <label>Destino</label>
          <select v-model="destinationMode">
            <option value="browser">Descarga normal del navegador</option>
            <option value="choose-folder">Elegir ubicación (si el navegador lo soporta)</option>
          </select>
        </div>
      </div>

      <div class="export-options">
        <label><input v-model="includeBackground" type="checkbox" /> Incluir Background</label>
        <label><input v-model="includeDescription" type="checkbox" /> Incluir descripción de la feature</label>
        <label><input v-model="includeTags" type="checkbox" /> Incluir tags</label>
      </div>

      <div class="export-shortcuts">
        <button class="secondary" type="button" @click="toggleAllScenarios(true)">Seleccionar todo</button>
        <button class="secondary" type="button" @click="toggleAllScenarios(false)">Deseleccionar todo</button>
        <button class="secondary" type="button" @click="selectOnlyOutlines">Solo Scenario Outline</button>
      </div>
    </div>

    <div class="card section-card export-card">
      <h3>Selección granular</h3>
      <p class="muted">Puedes exportar escenarios concretos y filas específicas dentro de cada bloque Examples.</p>

      <div v-for="scenario in sortedScenarios" :key="scenario.id" class="export-scenario">
        <label class="export-scenario-title">
          <input v-model="scenarioSelection[scenario.id]" type="checkbox" />
          <span>{{ scenario.name }} ({{ scenario.type }})</span>
        </label>

        <div v-if="scenario.type === 'OUTLINE' && scenarioSelection[scenario.id]" class="export-examples">
          <div v-for="table in scenario.exampleTables" :key="table.id" class="export-table-row">
            <div class="export-table-header">
              <strong>{{ table.name || 'Examples' }}</strong>
              <span class="muted">{{ selectedRowsForTable(table.id) }}/{{ table.rows.length }} filas</span>
              <button class="ghost" type="button" @click="toggleAllRowsForTable(table.id, true)">Todo</button>
              <button class="ghost" type="button" @click="toggleAllRowsForTable(table.id, false)">Ninguno</button>
            </div>

            <div class="export-row-grid">
              <label v-for="row in table.rows" :key="row.id" class="export-row-item">
                <input v-model="rowSelection[table.id][row.id]" type="checkbox" />
                <span>
                  {{ Object.values(row.values).slice(0, 2).join(' | ') || '(fila vacía)' }}
                </span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card section-card export-card export-footer">
      <span class="badge info">Escenarios: {{ selectedScenariosCount }}</span>
      <span class="badge neutral">Filas Examples: {{ selectedRowsCount }}</span>
      <button class="primary" type="button" :disabled="!canExport || saving" @click="exportSelection">
        {{ saving ? 'Exportando...' : 'Exportar selección' }}
      </button>
    </div>
  </section>
</template>
