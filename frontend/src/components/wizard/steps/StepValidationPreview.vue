<script setup lang="ts">
defineProps<{ errors: string[]; preview: string }>()
</script>
<template>
  <div>
    <div v-if="errors.length" class="validation">
      <ul><li v-for="error in errors" :key="error">{{ error }}</li></ul>
    </div>
    <pre class="preview">{{ preview }}</pre>
  </div>
</template>
