<script setup lang="ts">
defineProps<{ metrics: { features: number; scenarios: number; outlines: number; examples: number } }>()
</script>

<template>
  <div class="metrics">
    <div class="card metric-card">
      <p>Total Features</p>
      <strong>{{ metrics.features }}</strong>
    </div>
    <div class="card metric-card">
      <p>Scenarios</p>
      <strong>{{ metrics.scenarios }}</strong>
    </div>
    <div class="card metric-card">
      <p>Outlines</p>
      <strong>{{ metrics.outlines }}</strong>
    </div>
    <div class="card metric-card">
      <p>Rows en Examples</p>
      <strong>{{ metrics.examples }}</strong>
    </div>
  </div>
</template>
