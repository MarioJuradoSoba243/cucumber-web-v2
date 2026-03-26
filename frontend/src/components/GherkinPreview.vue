<script setup lang="ts">
defineProps<{ content: string }>()
</script>
<template>
  <pre class="preview">{{ content }}</pre>
</template>
