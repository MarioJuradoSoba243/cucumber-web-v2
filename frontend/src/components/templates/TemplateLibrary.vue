<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../../services/api'
import type { TemplateDocument } from '../../types/feature'

const emit = defineEmits<{ (e: 'create'): void; (e: 'edit', template: TemplateDocument): void; (e: 'apply', template: TemplateDocument): void }>()
const templates = ref<TemplateDocument[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  templates.value = await api.listTemplates()
  loading.value = false
}

async function remove(id?: string) {
  if (!id) return
  await api.deleteTemplate(id)
  await load()
}

onMounted(load)
defineExpose({ load })
</script>

<template>
  <div class="card section-card">
    <div class="topbar-actions">
      <h3>Plantillas</h3>
      <button class="secondary" @click="emit('create')">Crear desde cero</button>
    </div>
    <div v-if="loading">Cargando plantillas...</div>
    <div v-else-if="!templates.length" class="empty">No hay plantillas.</div>
    <ul v-else>
      <li v-for="tpl in templates" :key="tpl.id">
        <strong>{{ tpl.name }}</strong> ({{ tpl.scope }})
        <button class="ghost" @click="emit('edit', tpl)">Editar</button>
        <button class="ghost" @click="emit('apply', tpl)">Aplicar</button>
        <button class="ghost" @click="remove(tpl.id)">Eliminar</button>
      </li>
    </ul>
  </div>
</template>
