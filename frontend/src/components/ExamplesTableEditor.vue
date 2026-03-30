<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ExampleTable } from '../types/feature'

const props = defineProps<{ table: ExampleTable; placeholderColumns?: string[] }>()

const missingColumns = computed(() => {
  const source = props.placeholderColumns ?? []
  return source.filter((column) => !props.table.columns.includes(column))
})

const unusedColumns = computed(() => {
  const source = props.placeholderColumns ?? []
  return props.table.columns.filter((column) => !source.includes(column))
})

const emptyCells = computed(() => {
  return props.table.rows.flatMap((row) =>
    props.table.columns
      .filter((column) => !String(row.values[column] ?? '').trim())
      .map((column) => `${row.id.slice(0, 4)}:${column}`)
  )
})

const expandedEditor = ref<{ rowId: string; column: string } | null>(null)
const expandedValue = ref('')
const editingRowId = ref<string | null>(null)
const editingColumnId = ref<string | null>(null)
const editingColumnValue = ref('')

function addColumn() {
  const column = `column_${props.table.columns.length + 1}`
  props.table.columns.push(column)
  props.table.rows.forEach((row) => {
    row.values[column] = ''
  })
}

function removeColumn(column: string) {
  if (props.table.columns.length <= 1) return
  const index = props.table.columns.indexOf(column)
  if (index >= 0) {
    props.table.columns.splice(index, 1)
  }
  props.table.rows.forEach((row) => delete row.values[column])
}

function renameColumn(previous: string, next: string) {
  if (!next.trim()) return
  const index = props.table.columns.indexOf(previous)
  if (index < 0 || previous === next) return

  props.table.columns[index] = next
  props.table.rows.forEach((row) => {
    row.values[next] = row.values[previous] ?? ''
    delete row.values[previous]
  })
}

function startEditColumn(column: string) {
  editingColumnId.value = column
  editingColumnValue.value = column
}

function saveEditColumn(previous: string) {
  const next = editingColumnValue.value.trim()
  if (next) {
    renameColumn(previous, next)
  }
  editingColumnId.value = null
  editingColumnValue.value = ''
}

function cancelEditColumn() {
  editingColumnId.value = null
  editingColumnValue.value = ''
}

function addRow() {
  const values: Record<string, string> = {}
  props.table.columns.forEach((column) => {
    values[column] = ''
  })
  props.table.rows.push({ id: crypto.randomUUID(), values })
}

function removeRow(id: string) {
  const index = props.table.rows.findIndex((row) => row.id === id)
  if (index >= 0) {
    props.table.rows.splice(index, 1)
  }
}

function duplicateRow(id: string) {
  const index = props.table.rows.findIndex((row) => row.id === id)
  if (index < 0) return

  const sourceRow = props.table.rows[index]
  const values: Record<string, string> = {}
  props.table.columns.forEach((column) => {
    values[column] = sourceRow.values[column] ?? ''
  })

  props.table.rows.splice(index + 1, 0, {
    id: crypto.randomUUID(),
    values
  })
}

function editRow(id: string) {
  editingRowId.value = id
}

function cancelEditRow() {
  editingRowId.value = null
}

function saveEditRow() {
  editingRowId.value = null
}

function formatCellValue(value: string | undefined) {
  const normalized = String(value ?? '').trim()
  if (!normalized) return '—'
  if (normalized.length <= 20) return normalized
  return `${normalized.slice(0, 20)}…`
}

function openExpandedEditor(rowId: string, column: string) {
  const row = props.table.rows.find((entry) => entry.id === rowId)
  if (!row) return
  expandedEditor.value = { rowId, column }
  expandedValue.value = row.values[column] ?? ''
}

function closeExpandedEditor() {
  expandedEditor.value = null
}

function saveExpandedValue() {
  if (!expandedEditor.value) return
  const row = props.table.rows.find((entry) => entry.id === expandedEditor.value?.rowId)
  if (!row) return
  row.values[expandedEditor.value.column] = expandedValue.value
  closeExpandedEditor()
}
</script>

<template>
  <section class="examples-block">
    <div class="toolbar">
      <input v-model="table.name" placeholder="Nombre de Examples" />
      <div class="toolbar-actions">
        <button class="secondary" @click="addColumn">+ Columna</button>
        <button class="secondary" @click="addRow">+ Fila</button>
      </div>
    </div>

    <div class="validation-grid">
      <div class="validation-pill" :class="missingColumns.length ? 'error' : 'success'">
        {{ missingColumns.length ? `⚠ Faltan columnas (${missingColumns.length})` : '✓ Columnas requeridas OK' }}
      </div>
      <div class="validation-pill" :class="unusedColumns.length ? 'warning' : 'success'">
        {{ unusedColumns.length ? `ℹ Columnas sobrantes (${unusedColumns.length})` : '✓ Sin columnas sobrantes' }}
      </div>
      <div class="validation-pill" :class="emptyCells.length ? 'warning' : 'success'">
        {{ emptyCells.length ? `⚠ Celdas vacías (${emptyCells.length})` : '✓ Sin celdas vacías' }}
      </div>
    </div>

    <div class="table-wrap">
      <table>
        <colgroup>
          <col class="actions-col-width" />
          <col v-for="column in table.columns" :key="`col-${column}`" class="data-col-width" />
        </colgroup>
        <thead>
          <tr>
            <th class="action-col">Acciones</th>
            <th v-for="column in table.columns" :key="column">
              <div class="column-header-actions">
                <template v-if="editingColumnId === column">
                  <input v-model="editingColumnValue" />
                  <button class="ghost action-icon" title="Guardar nombre de columna" @click="saveEditColumn(column)">✓</button>
                  <button class="ghost action-icon" title="Cancelar edición de columna" @click="cancelEditColumn">✕</button>
                </template>
                <template v-else>
                  <span class="readonly-value" :title="column">{{ formatCellValue(column) }}</span>
                  <button class="ghost action-icon" title="Editar columna" @click="startEditColumn(column)">✏</button>
                  <button class="ghost danger action-icon" title="Eliminar columna" @click="removeColumn(column)">🗑</button>
                </template>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!table.rows.length">
            <td :colspan="table.columns.length + 1" class="empty-cell">No hay filas todavía. Añade una para comenzar.</td>
          </tr>
          <tr v-for="row in table.rows" :key="row.id">
            <td class="action-col compact-actions">
              <button
                v-if="editingRowId !== row.id"
                class="ghost action-icon"
                title="Editar fila"
                @click="editRow(row.id)"
              >
                ✏
              </button>
              <button
                v-else
                class="ghost action-icon"
                title="Guardar cambios"
                @click="saveEditRow"
              >
                ✓
              </button>
              <button
                v-if="editingRowId === row.id"
                class="ghost action-icon"
                title="Cancelar edición"
                @click="cancelEditRow"
              >
                ✕
              </button>
              <button class="ghost action-icon" title="Duplicar fila" @click="duplicateRow(row.id)">⎘</button>
              <button class="ghost danger action-icon" title="Eliminar fila" @click="removeRow(row.id)">🗑</button>
            </td>
            <td v-for="column in table.columns" :key="`${row.id}-${column}`" class="compact-cell">
              <div v-if="editingRowId === row.id" class="cell-input-wrap">
                <input v-model="row.values[column]" :placeholder="`Valor para ${column}`" />
                <button class="ghost icon-btn" type="button" title="Expandir editor" @click="openExpandedEditor(row.id, column)">⤢</button>
              </div>
              <span v-else class="readonly-value" :title="String(row.values[column] ?? '')">{{ formatCellValue(row.values[column]) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="expandedEditor" class="modal-backdrop" @click.self="closeExpandedEditor">
      <div class="modal card">
        <header class="modal-header">
          <h4>Editar valor completo</h4>
          <button class="ghost icon-btn" type="button" @click="closeExpandedEditor">✕</button>
        </header>
        <p class="muted">Columna: <strong>{{ expandedEditor.column }}</strong></p>
        <textarea v-model="expandedValue" rows="8" placeholder="Escribe el texto completo..." />
        <div class="modal-actions">
          <button class="ghost" type="button" @click="closeExpandedEditor">Cancelar</button>
          <button class="primary" type="button" @click="saveExpandedValue">Guardar valor</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.table-wrap table {
  font-size: 0.8rem;
  table-layout: fixed;
  border-collapse: collapse;
}

.table-wrap th,
.table-wrap td {
  padding: 0.15rem 0.25rem;
  vertical-align: middle;
  text-align: left;
}

.table-wrap th input {
  max-width: 105px;
  font-size: 0.75rem;
}

.compact-cell {
  width: 100%;
}

.readonly-value {
  display: inline-block;
  max-width: 108px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.78rem;
}

.compact-actions {
  width: 100%;
  white-space: nowrap;
}

.action-icon {
  width: 22px;
  height: 22px;
  min-width: 22px;
  padding: 0;
  margin-right: 0.15rem;
  font-size: 0.72rem;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.column-header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 0.1rem;
  width: 100%;
}

.actions-col-width {
  width: 78px;
}

.data-col-width {
  width: 112px;
}
</style>
