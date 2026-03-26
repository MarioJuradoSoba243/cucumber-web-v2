import axios from 'axios'
import type { FeatureDocument, FeatureSummary } from '../types/feature'

const http = axios.create({ baseURL: '/api' })

export const api = {
  async listFeatures(query = ''): Promise<FeatureSummary[]> {
    const { data } = await http.get('/features', { params: { q: query } })
    return data
  },
  async getFeature(id: string): Promise<FeatureDocument> {
    const { data } = await http.get(`/features/${encodeURIComponent(id)}`)
    return data
  },
  async updateFeature(feature: FeatureDocument): Promise<FeatureDocument> {
    const { data } = await http.put(`/features/${encodeURIComponent(feature.id)}`, feature)
    return data
  },
  async createFeature(feature: FeatureDocument): Promise<FeatureDocument> {
    const { data } = await http.post('/features', feature)
    return data
  },
  async deleteFeature(id: string): Promise<void> {
    await http.delete(`/features/${encodeURIComponent(id)}`)
  },
  async exportFeature(id: string): Promise<string> {
    const { data } = await http.get(`/features/${encodeURIComponent(id)}/export`)
    return data
  },
  async settings(): Promise<{ featuresPath: string }> {
    const { data } = await http.get('/settings/features-path')
    return data
  }
}
