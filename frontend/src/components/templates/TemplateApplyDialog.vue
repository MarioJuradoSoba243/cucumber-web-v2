<script setup lang="ts">
import { ref, watch } from 'vue'
import { api } from '../../services/api'
import type { TemplateDocument } from '../../types/feature'

const props = defineProps<{ template: TemplateDocument | null }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'applied', preview: string): void }>()
const preview = ref('')
const placeholders = ref<Record<string, string>>({})

watch(
  () => props.template,
  () => {
    preview.value = ''
    placeholders.value = {}
  }
)

async function apply() {
  if (!props.template?.id) return
  const result = await api.applyTemplate(props.template.id, placeholders.value)
  preview.value = result.preview
  emit('applied', result.preview)
}
</script>

<template>
  <div v-if="template" class="card section-card">
    <h4>Aplicar plantilla: {{ template.name }}</h4>
    <input placeholder="placeholder valor (key=value)" @change="(e) => { const [k,v] = (e.target as HTMLInputElement).value.split('='); if(k&&v) placeholders[k.trim()] = v.trim() }" />
    <button class="primary" @click="apply">Previsualizar aplicación</button>
    <pre v-if="preview" class="preview">{{ preview }}</pre>
    <button class="ghost" @click="emit('close')">Cerrar</button>
  </div>
</template>
