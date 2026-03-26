<script setup lang="ts">
import type { ExampleTable } from '../types/feature'

const props = defineProps<{ table: ExampleTable }>()

function addColumn() {
  const column = `column_${props.table.columns.length + 1}`
  props.table.columns.push(column)
  props.table.rows.forEach((row) => (row.values[column] = ''))
}

function removeColumn(column: string) {
  if (props.table.columns.length <= 1) return
  props.table.columns = props.table.columns.filter((c) => c !== column)
  props.table.rows.forEach((row) => delete row.values[column])
}

function addRow() {
  const values: Record<string, string> = {}
  props.table.columns.forEach((c) => (values[c] = ''))
  props.table.rows.push({ id: crypto.randomUUID(), values })
}

function removeRow(id: string) {
  props.table.rows = props.table.rows.filter((r) => r.id !== id)
}
</script>

<template>
  <section class="examples-block">
    <div class="toolbar">
      <input v-model="table.name" placeholder="Nombre Examples" />
      <button @click="addColumn">+ Columna</button>
      <button @click="addRow">+ Fila</button>
    </div>
    <table>
      <thead>
        <tr>
          <th v-for="column in table.columns" :key="column">
            <input v-model="table.columns[table.columns.indexOf(column)]" />
            <button class="danger" @click="removeColumn(column)">x</button>
          </th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in table.rows" :key="row.id">
          <td v-for="column in table.columns" :key="column">
            <input v-model="row.values[column]" />
          </td>
          <td><button class="danger" @click="removeRow(row.id)">Eliminar</button></td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
