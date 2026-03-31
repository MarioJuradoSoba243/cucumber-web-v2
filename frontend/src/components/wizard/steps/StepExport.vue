<script setup lang="ts">
defineProps<{ doneMessage: string }>()
</script>
<template>
  <div>
    <p class="muted">Usa el botón global de Export para descargar la feature.</p>
    <p v-if="doneMessage" class="ok">{{ doneMessage }}</p>
  </div>
</template>
