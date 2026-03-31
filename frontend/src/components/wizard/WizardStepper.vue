<script setup lang="ts">
import type { StepStatus, WizardStep } from '../../types/feature'

defineProps<{ activeStep: WizardStep; states: Record<WizardStep, StepStatus> }>()
const emit = defineEmits<{ (e: 'jump', step: WizardStep): void }>()
const steps: { key: WizardStep; label: string }[] = [
  { key: 'feature', label: 'Feature' },
  { key: 'scenarios', label: 'Scenarios' },
  { key: 'examples', label: 'Examples' },
  { key: 'validation', label: 'Validación' },
  { key: 'export', label: 'Exportación' }
]
</script>

<template>
  <div class="wizard-stepper">
    <button v-for="step in steps" :key="step.key" :class="['step-btn', states[step.key], { active: activeStep === step.key }]" @click="emit('jump', step.key)">
      {{ step.label }}
    </button>
  </div>
</template>
