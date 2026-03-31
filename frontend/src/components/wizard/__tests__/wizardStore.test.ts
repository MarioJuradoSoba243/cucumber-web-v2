import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useWizardStore } from '../../../stores/wizardStore'
import { api } from '../../../services/api'

vi.mock('../../../services/api', () => ({
  api: {
    validateFeatureStep: vi.fn()
  }
}))

describe('wizardStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('bloquea avance si la validación retorna blocking', async () => {
    vi.mocked(api.validateFeatureStep).mockResolvedValue({ errors: ['error'], warnings: [], blocking: true })
    const store = useWizardStore()
    const valid = await store.validateCurrent({ id: 'id', name: '', description: '', filePath: '', tags: [], scenarios: [], validation: { errors: [], warnings: [], missingColumns: [], unusedColumns: [], emptyCells: [] } })
    expect(valid).toBe(false)
    expect(store.stepsState.feature).toBe('error')
  })

  it('restaura progreso desde localStorage', () => {
    localStorage.setItem('wizard:test.feature', JSON.stringify({ activeStep: 'examples', stepsState: { feature: 'completed', scenarios: 'completed', examples: 'in_progress', validation: 'pending', export: 'pending' } }))
    const store = useWizardStore()
    store.hydrate('test.feature')
    expect(store.activeStep).toBe('examples')
    expect(store.stepsState.feature).toBe('completed')
  })
})
