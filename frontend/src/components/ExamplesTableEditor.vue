<script setup lang="ts">
import { computed } from 'vue'
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
        <thead>
          <tr>
            <th v-for="column in table.columns" :key="column">
              <input :value="column" @change="renameColumn(column, ($event.target as HTMLInputElement).value)" />
              <button class="ghost icon-btn" title="Eliminar columna" @click="removeColumn(column)">✕</button>
            </th>
            <th class="action-col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!table.rows.length">
            <td :colspan="table.columns.length + 1" class="empty-cell">No hay filas todavía. Añade una para comenzar.</td>
          </tr>
          <tr v-for="row in table.rows" :key="row.id">
            <td v-for="column in table.columns" :key="`${row.id}-${column}`">
              <input v-model="row.values[column]" :placeholder="`Valor para ${column}`" />
            </td>
            <td class="action-col">
              <button class="ghost danger" @click="removeRow(row.id)">🗑 Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
