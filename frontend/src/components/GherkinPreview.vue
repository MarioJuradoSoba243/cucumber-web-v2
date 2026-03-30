<script setup lang="ts">
defineProps<{ content: string }>()
</script>

<template>
  <div class="preview-wrap card">
    <div class="preview-header">
      <h3>Vista previa Gherkin</h3>
      <span class="badge neutral">Live</span>
    </div>
    <pre class="preview">{{ content }}</pre>
  </div>
</template>
