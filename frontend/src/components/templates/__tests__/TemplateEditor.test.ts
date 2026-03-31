import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TemplateEditor from '../TemplateEditor.vue'
import { api } from '../../../services/api'

vi.mock('../../../services/api', () => ({
  api: { createTemplate: vi.fn().mockResolvedValue({}), updateTemplate: vi.fn().mockResolvedValue({}) }
}))

describe('TemplateEditor', () => {
  it('crea plantilla cuando no hay id', async () => {
    const wrapper = mount(TemplateEditor, {
      props: { modelValue: { name: 'T', description: '', tags: [], scope: 'SCENARIO', content: 'Scenario: <a>' } }
    })
    await wrapper.find('button.primary').trigger('click')
    expect(api.createTemplate).toHaveBeenCalled()
  })
})
