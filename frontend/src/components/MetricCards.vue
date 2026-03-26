<script setup lang="ts">
defineProps<{ metrics: { features: number; scenarios: number; outlines: number; examples: number } }>()
</script>

<template>
  <div class="metrics">
    <div class="card"><h4>Features</h4><strong>{{ metrics.features }}</strong></div>
    <div class="card"><h4>Scenarios</h4><strong>{{ metrics.scenarios }}</strong></div>
    <div class="card"><h4>Outlines</h4><strong>{{ metrics.outlines }}</strong></div>
    <div class="card"><h4>Examples Rows</h4><strong>{{ metrics.examples }}</strong></div>
  </div>
</template>
