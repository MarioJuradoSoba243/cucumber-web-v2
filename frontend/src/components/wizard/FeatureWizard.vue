<script setup lang="ts">
import { computed, watch } from 'vue'
import { useWizardStore } from '../../stores/wizardStore'
import { useGherkinPreview } from '../../composables/useGherkinPreview'
import type { FeatureDocument } from '../../types/feature'
import WizardStepper from './WizardStepper.vue'
import StepFeatureInfo from './steps/StepFeatureInfo.vue'
import StepScenarios from './steps/StepScenarios.vue'
import StepExamples from './steps/StepExamples.vue'
import StepValidationPreview from './steps/StepValidationPreview.vue'
import StepExport from './steps/StepExport.vue'

const props = defineProps<{ feature: FeatureDocument; exportedMessage: string }>()
const emit = defineEmits<{ (e: 'save-draft'): void }>()
const wizard = useWizardStore()
const { render } = useGherkinPreview()

watch(
  () => props.feature.id,
  (id) => {
    if (!id) return
    wizard.hydrate(id)
    wizard.initializeFromFeature(props.feature)
  },
  { immediate: true }
)

watch(
  () => [props.feature.id, wizard.activeStep, wizard.stepsState],
  () => {
    if (!props.feature?.id) return
    wizard.persist(props.feature.id)
  },
  { deep: true }
)

const preview = computed(() => render(props.feature))

async function next() {
  const valid = await wizard.validateCurrent(props.feature)
  if (valid) wizard.next()
}
</script>

<template>
  <div class="card section-card">
    <h3>Wizard de edición</h3>
    <p class="muted">Progreso: {{ wizard.progress }}%</p>
    <WizardStepper :active-step="wizard.activeStep" :states="wizard.stepsState" @jump="wizard.goTo" />

    <StepFeatureInfo v-if="wizard.activeStep === 'feature'" :feature="feature" />
    <StepScenarios v-if="wizard.activeStep === 'scenarios'" :feature="feature" />
    <StepExamples v-if="wizard.activeStep === 'examples'" :feature="feature" />
    <StepValidationPreview v-if="wizard.activeStep === 'validation'" :errors="wizard.errors.validation" :preview="preview" />
    <StepExport v-if="wizard.activeStep === 'export'" :done-message="exportedMessage" />

    <div class="quick-actions">
      <button class="ghost" @click="wizard.previous">Anterior</button>
      <button class="secondary" @click="emit('save-draft')">Guardar borrador</button>
      <button class="primary" @click="next">Siguiente</button>
    </div>
  </div>
</template>
