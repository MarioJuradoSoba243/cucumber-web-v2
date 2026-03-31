import axios from 'axios'
import type {
  ExportSelectionRequest,
  FeatureDocument,
  FeatureSummary,
  SearchPage,
  SearchType,
  StepValidationResponse,
  TemplateDocument
} from '../types/feature'

const http = axios.create({ baseURL: '/api' })

export const api = {
  async listFeatures(query = ''): Promise<FeatureSummary[]> {
    const cleanQuery = query.trim()
    const params = cleanQuery ? { q: cleanQuery } : undefined
    const { data } = await http.get('/features', { params })
    return data
  },
  async getFeature(id: string): Promise<FeatureDocument> {
    const { data } = await http.get('/features/detail', { params: { id } })
    return data
  },
  async updateFeature(feature: FeatureDocument): Promise<FeatureDocument> {
    const { data } = await http.put('/features/detail', feature, { params: { id: feature.id } })
    return data
  },
  async createFeature(feature: FeatureDocument): Promise<FeatureDocument> {
    const { data } = await http.post('/features', feature)
    return data
  },
  async deleteFeature(id: string): Promise<void> {
    await http.delete('/features/detail', { params: { id } })
  },
  async exportFeature(id: string): Promise<string> {
    const { data } = await http.get('/features/export', { params: { id } })
    return data
  },
  async exportSelection(payload: ExportSelectionRequest): Promise<string> {
    const { data } = await http.post('/features/export', payload)
    return data
  },
  async settings(): Promise<{ featuresPath: string }> {
    const { data } = await http.get('/settings/features-path')
    return data
  },
  async validateFeatureStep(feature: FeatureDocument, step: 'feature' | 'scenarios' | 'examples' | 'final'): Promise<StepValidationResponse> {
    const { data } = await http.post('/features/validate', feature, { params: { step } })
    return data
  },
  async search(params: { q: string; types?: SearchType[]; tags?: string[]; path?: string; page?: number; size?: number }): Promise<SearchPage> {
    const { data } = await http.get('/search', { params })
    return data
  },
  async listTemplates(): Promise<TemplateDocument[]> {
    const { data } = await http.get('/templates')
    return data
  },
  async getTemplate(id: string): Promise<TemplateDocument> {
    const { data } = await http.get(`/templates/${id}`)
    return data
  },
  async createTemplate(payload: TemplateDocument): Promise<TemplateDocument> {
    const { data } = await http.post('/templates', payload)
    return data
  },
  async updateTemplate(id: string, payload: TemplateDocument): Promise<TemplateDocument> {
    const { data } = await http.put(`/templates/${id}`, payload)
    return data
  },
  async deleteTemplate(id: string): Promise<void> {
    await http.delete(`/templates/${id}`)
  },
  async applyTemplate(id: string, placeholders: Record<string, string>): Promise<{ scope: string; preview: string; placeholders: Record<string, string> }> {
    const { data } = await http.post(`/templates/${id}/apply`, { placeholders })
    return data
  }
}
