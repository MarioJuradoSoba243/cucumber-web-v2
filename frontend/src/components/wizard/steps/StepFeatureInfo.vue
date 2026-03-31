<script setup lang="ts">
import type { FeatureDocument } from '../../../types/feature'
defineProps<{ feature: FeatureDocument }>()
</script>
<template>
  <div>
    <label>Nombre</label>
    <input v-model="feature.name" />
    <label>Descripción</label>
    <textarea v-model="feature.description" rows="3" />
    <label>Tags (coma separada)</label>
    <input :value="feature.tags.join(', ')" @input="feature.tags = ($event.target as HTMLInputElement).value.split(',').map((v) => v.trim()).filter(Boolean)" />
  </div>
</template>
