import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '../services/api'
import type { FeatureDocument, StepStatus, WizardStep } from '../types/feature'

const ORDER: WizardStep[] = ['feature', 'scenarios', 'examples', 'validation', 'export']

export const useWizardStore = defineStore('wizard', () => {
  const activeStep = ref<WizardStep>('feature')
  const stepsState = ref<Record<WizardStep, StepStatus>>({
    feature: 'pending',
    scenarios: 'pending',
    examples: 'pending',
    validation: 'pending',
    export: 'pending'
  })
  const errors = ref<Record<WizardStep, string[]>>({ feature: [], scenarios: [], examples: [], validation: [], export: [] })

  const progress = computed(() => {
    const completed = Object.values(stepsState.value).filter((status) => status === 'completed').length
    return Math.round((completed / ORDER.length) * 100)
  })

  function hydrate(featureId: string) {
    const raw = localStorage.getItem(`wizard:${featureId}`)
    if (!raw) return
    const parsed = JSON.parse(raw)
    activeStep.value = parsed.activeStep ?? 'feature'
    stepsState.value = parsed.stepsState ?? stepsState.value
  }

  function persist(featureId: string) {
    localStorage.setItem(
      `wizard:${featureId}`,
      JSON.stringify({
        activeStep: activeStep.value,
        stepsState: stepsState.value
      })
    )
  }

  function initializeFromFeature(feature: FeatureDocument) {
    const firstIncomplete = !feature.name?.trim()
      ? 'feature'
      : feature.scenarios.length === 0
        ? 'scenarios'
        : feature.scenarios.some((scenario) => scenario.type === 'OUTLINE' && scenario.exampleTables.length === 0)
          ? 'examples'
          : 'validation'
    activeStep.value = firstIncomplete
  }

  async function validateCurrent(feature: FeatureDocument): Promise<boolean> {
    const current = activeStep.value
    stepsState.value[current] = 'in_progress'
    const mappedStep = current === 'validation' || current === 'export' ? 'final' : current
    const result = await api.validateFeatureStep(feature, mappedStep)
    errors.value[current] = result.errors
    stepsState.value[current] = result.blocking ? 'error' : 'completed'
    return !result.blocking
  }

  function goTo(step: WizardStep) {
    activeStep.value = step
    if (stepsState.value[step] === 'pending') {
      stepsState.value[step] = 'in_progress'
    }
  }

  function next() {
    const index = ORDER.indexOf(activeStep.value)
    if (index < ORDER.length - 1) {
      activeStep.value = ORDER[index + 1]
    }
  }

  function previous() {
    const index = ORDER.indexOf(activeStep.value)
    if (index > 0) {
      activeStep.value = ORDER[index - 1]
    }
  }

  return { activeStep, stepsState, errors, progress, hydrate, persist, initializeFromFeature, validateCurrent, goTo, next, previous }
})
