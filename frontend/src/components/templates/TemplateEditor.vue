<script setup lang="ts">
import { ref, watch } from 'vue'
import { api } from '../../services/api'
import type { TemplateDocument } from '../../types/feature'

const props = defineProps<{ modelValue: TemplateDocument | null }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: TemplateDocument | null): void; (e: 'saved'): void }>()
const draft = ref<TemplateDocument>({ name: '', description: '', tags: [], scope: 'SCENARIO', content: '' })

watch(
  () => props.modelValue,
  (value) => {
    draft.value = value ? { ...value } : { name: '', description: '', tags: [], scope: 'SCENARIO', content: '' }
  },
  { immediate: true }
)

async function save() {
  if (draft.value.id) {
    await api.updateTemplate(draft.value.id, draft.value)
  } else {
    await api.createTemplate(draft.value)
  }
  emit('saved')
  emit('update:modelValue', null)
}
</script>

<template>
  <div v-if="modelValue" class="card section-card">
    <h4>Editor de plantilla</h4>
    <input v-model="draft.name" placeholder="Nombre" />
    <textarea v-model="draft.description" placeholder="Descripción" />
    <select v-model="draft.scope">
      <option value="FEATURE">Feature</option>
      <option value="SCENARIO">Scenario</option>
      <option value="OUTLINE">Outline</option>
    </select>
    <textarea v-model="draft.content" rows="8" placeholder="Contenido de plantilla" />
    <button class="primary" @click="save">Guardar</button>
    <button class="ghost" @click="emit('update:modelValue', null)">Cancelar</button>
  </div>
</template>
