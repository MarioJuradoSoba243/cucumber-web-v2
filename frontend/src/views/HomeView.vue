<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useFeatureStore } from '../stores/featureStore'
import FeatureSidebar from '../components/FeatureSidebar.vue'
import MetricCards from '../components/MetricCards.vue'
import ScenarioEditor from '../components/ScenarioEditor.vue'
import GherkinPreview from '../components/GherkinPreview.vue'
import ExportManager from '../components/ExportManager.vue'
import FeatureWizard from '../components/wizard/FeatureWizard.vue'
import GlobalSearch from '../components/search/GlobalSearch.vue'
import TemplateLibrary from '../components/templates/TemplateLibrary.vue'
import TemplateEditor from '../components/templates/TemplateEditor.vue'
import TemplateApplyDialog from '../components/templates/TemplateApplyDialog.vue'
import { useGherkinPreview } from '../composables/useGherkinPreview'
import type { TemplateDocument } from '../types/feature'

const store = useFeatureStore()
const activeTab = ref<'overview' | 'scenarios' | 'examples' | 'preview' | 'export'>('overview')
const { render } = useGherkinPreview()
const sidebarCollapsed = ref(false)
const darkMode = ref(false)
const exportedMessage = ref('')
const editingTemplate = ref<TemplateDocument | null>(null)
const applyingTemplate = ref<TemplateDocument | null>(null)
const showSearchPanel = ref(false)
const showTemplatePanel = ref(false)
const showWizardModal = ref(false)
const baseline = ref('')

onMounted(() => {
  store.loadFeatures()
  document.body.classList.toggle('dark-theme', darkMode.value)
})

watch(darkMode, (enabled) => {
  document.body.classList.toggle('dark-theme', enabled)
})

onBeforeUnmount(() => {
  document.body.classList.remove('dark-theme')
})

watch(
  () => store.features,
  (list) => {
    if (list.length && !store.selectedFeature) {
      store.openFeature(list[0].id)
    }
  },
  { deep: true }
)

watch(
  () => store.selectedFeature,
  (feature) => {
    baseline.value = feature ? JSON.stringify(feature) : ''
  },
  { deep: false }
)

const preview = computed(() => render(store.selectedFeature))

const hasPendingChanges = computed(() => {
  if (!store.selectedFeature) return false
  return JSON.stringify(store.selectedFeature) !== baseline.value
})

const outlineScenarios = computed(() => store.selectedFeature?.scenarios.filter((scenario) => scenario.type === 'OUTLINE') ?? [])

function open(id: string) {
  store.openFeature(id)
  exportedMessage.value = ''
}

function addScenario(type: 'SCENARIO' | 'OUTLINE') {
  if (!store.selectedFeature) return
  store.selectedFeature.scenarios.push({
    id: crypto.randomUUID(),
    type,
    name: type === 'OUTLINE' ? 'Nuevo Scenario Outline' : 'Nuevo Scenario',
    tags: [],
    steps: [],
    exampleTables: type === 'OUTLINE' ? [{ id: crypto.randomUUID(), name: 'Default', tags: [], columns: ['param'], rows: [] }] : []
  })
}

function handleCreateFeature(name: string) {
  store.createFeature(name)
}

function selectSearchResult(payload: { featureId: string; field: string }) {
  store.openFeature(payload.featureId)
  if (payload.field === 'name' || payload.field === 'description') {
    activeTab.value = 'overview'
  } else if (payload.field === 'step') {
    activeTab.value = 'scenarios'
  } else if (payload.field === 'column' || payload.field === 'value') {
    activeTab.value = 'examples'
  }
}

async function saveFeature() {
  await store.saveFeature()
  if (store.selectedFeature) {
    baseline.value = JSON.stringify(store.selectedFeature)
  }
}

</script>

<template>
  <div :class="['layout', { dark: darkMode }]">
    <FeatureSidebar
      :items="store.features"
      :selected-id="store.selectedFeature?.id"
      :loading="store.loading"
      :query="store.query"
      :collapsed="sidebarCollapsed"
      @select="open"
      @search="(value) => { store.query = value; store.loadFeatures() }"
      @create="handleCreateFeature"
      @toggle="sidebarCollapsed = !sidebarCollapsed"
    />

    <main class="main">
      <header class="topbar card">
        <div>
          <h1>{{ store.selectedFeature?.name || 'Selecciona una feature' }}</h1>
          <p class="muted">Gestiona escenarios Cucumber con validaciones en tiempo real.</p>
        </div>

        <div class="topbar-actions">
          <span class="badge" :class="hasPendingChanges ? 'warning' : 'success'">
            {{ hasPendingChanges ? 'Cambios pendientes' : 'Guardado' }}
          </span>
          <button class="ghost" @click="darkMode = !darkMode">{{ darkMode ? '☀️ Modo claro' : '🌙 Modo oscuro' }}</button>
          <button class="secondary" :disabled="!store.selectedFeature" @click="activeTab = 'export'">Exportar</button>
          <button class="secondary" @click="showSearchPanel = true">Búsqueda global</button>
          <button class="secondary" @click="showTemplatePanel = true">Plantillas</button>
          <button class="secondary" :disabled="!store.selectedFeature" @click="showWizardModal = true">Abrir wizard</button>
          <button class="primary" :disabled="!store.selectedFeature" @click="saveFeature">Guardar</button>
        </div>
      </header>

      <p v-if="store.message" class="ok">{{ store.message }}</p>
      <p v-if="exportedMessage" class="ok">{{ exportedMessage }}</p>
      <p v-if="store.error" class="validation">{{ store.error }}</p>

      <MetricCards :metrics="store.metrics" />
      <div v-if="store.selectedFeature" class="panel card">
        <div class="tabs">
          <button :class="{ active: activeTab === 'overview' }" @click="activeTab = 'overview'">Overview</button>
          <button :class="{ active: activeTab === 'scenarios' }" @click="activeTab = 'scenarios'">Scenarios</button>
          <button :class="{ active: activeTab === 'examples' }" @click="activeTab = 'examples'">Examples</button>
          <button :class="{ active: activeTab === 'export' }" @click="activeTab = 'export'">Export</button>
          <button :class="{ active: activeTab === 'preview' }" @click="activeTab = 'preview'">Preview</button>
        </div>

        <section v-if="activeTab === 'overview'" class="tab-content fade-in">
          <div class="card section-card">
            <label>Nombre de la feature</label>
            <input v-model="store.selectedFeature.name" class="feature-title" placeholder="Nombre de la feature" />
            <label>Descripción</label>
            <textarea v-model="store.selectedFeature.description" rows="4" placeholder="Describe el objetivo de esta feature" />
            <p class="muted">Ruta: {{ store.selectedFeature.filePath || '(sin ruta todavía)' }}</p>
          </div>

          <div class="card section-card">
            <h3>Acciones rápidas</h3>
            <div class="quick-actions">
              <button class="secondary" @click="addScenario('SCENARIO')">+ Scenario</button>
              <button class="secondary" @click="addScenario('OUTLINE')">+ Scenario Outline</button>
            </div>
          </div>

          <div v-if="store.selectedFeature.validation.errors.length" class="validation">
            <strong>Errores de validación</strong>
            <ul>
              <li v-for="err in store.selectedFeature.validation.errors" :key="err">{{ err }}</li>
            </ul>
          </div>
        </section>

        <section v-if="activeTab === 'scenarios'" class="tab-content fade-in">
          <ScenarioEditor v-for="scenario in store.selectedFeature.scenarios" :key="scenario.id" :scenario="scenario" />
          <div v-if="!store.selectedFeature.scenarios.length" class="empty">No hay escenarios todavía. Añade uno desde Overview.</div>
        </section>

        <section v-if="activeTab === 'examples'" class="tab-content fade-in">
          <p class="muted">Edita solo las tablas de datos de cada Scenario Outline.</p>
          <ScenarioEditor v-for="scenario in outlineScenarios" :key="scenario.id" :scenario="scenario" mode="examples" />
          <div v-if="!outlineScenarios.length" class="empty">No hay Scenario Outline. Crea uno para editar Examples.</div>
        </section>

        <section v-if="activeTab === 'preview'" class="tab-content fade-in">
          <GherkinPreview :content="preview" />
        </section>

        <section v-if="activeTab === 'export'" class="tab-content fade-in">
          <ExportManager :feature="store.selectedFeature" @done="(msg) => { exportedMessage = msg }" />
        </section>
      </div>

      <div v-else class="empty card">Selecciona una feature para empezar.</div>

      <teleport to="body">
        <div v-if="showSearchPanel" class="overlay" @click.self="showSearchPanel = false">
          <div class="overlay-panel card">
            <div class="topbar-actions">
              <h3>Búsqueda global</h3>
              <button class="ghost" @click="showSearchPanel = false">Cerrar</button>
            </div>
            <GlobalSearch @select="(payload) => { selectSearchResult(payload); showSearchPanel = false }" />
          </div>
        </div>
      </teleport>

      <teleport to="body">
        <div v-if="showTemplatePanel" class="overlay" @click.self="showTemplatePanel = false">
          <div class="overlay-panel card">
            <div class="topbar-actions">
              <h3>Biblioteca de plantillas</h3>
              <button class="ghost" @click="showTemplatePanel = false">Cerrar</button>
            </div>
            <TemplateLibrary @create="editingTemplate = { name: '', description: '', tags: [], scope: 'SCENARIO', content: '' }" @edit="(tpl) => editingTemplate = tpl" @apply="(tpl) => applyingTemplate = tpl" />
            <TemplateEditor v-model="editingTemplate" @saved="store.message = 'Plantilla guardada'" />
            <TemplateApplyDialog :template="applyingTemplate" @close="applyingTemplate = null" @applied="(preview) => { if(store.selectedFeature) { store.selectedFeature.description = `${store.selectedFeature.description}\\n${preview}`; applyingTemplate = null } }" />
          </div>
        </div>
      </teleport>

      <teleport to="body">
        <div v-if="showWizardModal && store.selectedFeature" class="overlay" @click.self="showWizardModal = false">
          <div class="overlay-panel card">
            <div class="topbar-actions">
              <h3>Wizard de edición</h3>
              <button class="ghost" @click="showWizardModal = false">Cerrar</button>
            </div>
            <FeatureWizard :feature="store.selectedFeature" :exported-message="exportedMessage" @save-draft="saveFeature" />
          </div>
        </div>
      </teleport>
    </main>
  </div>
</template>
