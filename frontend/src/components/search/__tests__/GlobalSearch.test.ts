import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import GlobalSearch from '../GlobalSearch.vue'
import { api } from '../../../services/api'

vi.mock('../../../services/api', () => ({
  api: { search: vi.fn().mockResolvedValue({ results: [{ featureId: 'f1', featureName: 'Feature', filePath: 'a.feature', hits: [{ type: 'feature', text: 'Login', highlight: '<mark>Login</mark>', score: 1, location: { featureId: 'f1', field: 'name' } }] }], total: 1, page: 0, size: 10 }) }
}))

describe('GlobalSearch', () => {
  it('renderiza highlights tras debounce', async () => {
    vi.useFakeTimers()
    const wrapper = mount(GlobalSearch)
    await wrapper.find('#global-search-input').setValue('login')
    vi.advanceTimersByTime(350)
    await Promise.resolve()
    await Promise.resolve()
    await wrapper.vm.$nextTick()
    expect(api.search).toHaveBeenCalled()
    expect(wrapper.html()).toContain('<mark>Login</mark>')
    vi.useRealTimers()
  })
})
