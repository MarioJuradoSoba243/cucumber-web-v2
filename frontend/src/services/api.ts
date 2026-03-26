import axios from 'axios'
import type { FeatureDocument, FeatureSummary } from '../types/feature'

const http = axios.create({ baseURL: '/api' })

export const api = {
  async listFeatures(query = ''): Promise<FeatureSummary[]> {
    const { data } = await http.get('/features', { params: { q: query } })
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
  async settings(): Promise<{ featuresPath: string }> {
    const { data } = await http.get('/settings/features-path')
    return data
  }
}
