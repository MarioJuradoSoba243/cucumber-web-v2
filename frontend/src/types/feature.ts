export type Step = { keyword: string; text: string }

export type ExampleRow = { id: string; values: Record<string, string> }

export type ExampleTable = {
  id: string
  name?: string
  tags: string[]
  columns: string[]
  rows: ExampleRow[]
}

export type Scenario = {
  id: string
  type: 'SCENARIO' | 'OUTLINE'
  name: string
  tags: string[]
  steps: Step[]
  exampleTables: ExampleTable[]
}

export type FeatureValidation = {
  errors: string[]
  warnings: string[]
  missingColumns: string[]
  unusedColumns: string[]
  emptyCells: string[]
}

export type FeatureDocument = {
  id: string
  filePath: string
  name: string
  description: string
  tags: string[]
  background?: { name: string; steps: Step[] }
  scenarios: Scenario[]
  validation: FeatureValidation
}

export type FeatureSummary = {
  id: string
  name: string
  filePath: string
  description: string
  tags: string[]
  scenarioCount: number
  outlineCount: number
  totalExampleRows: number
}

export type ScenarioExportSelection = {
  scenarioId: string
  selected: boolean
  exampleRowIdsByTableId: Record<string, string[]>
}

export type ExportSelectionRequest = {
  feature: FeatureDocument
  scenarios: ScenarioExportSelection[]
}

export type SearchType = 'feature' | 'scenario' | 'outline' | 'example'

export type SearchHit = {
  type: SearchType
  text: string
  highlight: string
  score: number
  location: { featureId: string; scenarioId?: string; tableId?: string; rowId?: string; field: string }
}

export type SearchResult = { featureId: string; featureName: string; filePath: string; hits: SearchHit[] }

export type SearchPage = { results: SearchResult[]; total: number; page: number; size: number }
