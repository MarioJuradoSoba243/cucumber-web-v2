import axios from 'axios'
import type {
  ExportSelectionRequest,
  FeatureDocument,
  DirectoryNode,
  FeatureSummary,
  SearchPage,
  SearchType
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
  async getFoldersTree(): Promise<DirectoryNode> {
    const { data } = await http.get('/folders/tree')
    return data
  },
  async createFolder(parentPath: string, name: string): Promise<DirectoryNode> {
    const { data } = await http.post('/folders', { parentPath, name })
    return data
  },
  async renamePath(path: string, newName: string): Promise<DirectoryNode> {
    const { data } = await http.put('/paths/rename', { path, newName })
    return data
  },
  async movePath(sourcePath: string, destinationFolderPath: string): Promise<DirectoryNode> {
    const { data } = await http.post('/paths/move', { sourcePath, destinationFolderPath })
    return data
  },
  async search(params: { q: string; types?: SearchType[]; tags?: string[]; path?: string; page?: number; size?: number }): Promise<SearchPage> {
    const { data } = await http.get('/search', { params })
    return data
  }
}
