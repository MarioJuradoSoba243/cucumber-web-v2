<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useFeatureStore } from '../stores/featureStore'
import FeatureSidebar from '../components/FeatureSidebar.vue'
import MetricCards from '../components/MetricCards.vue'
import ScenarioEditor from '../components/ScenarioEditor.vue'
import GherkinPreview from '../components/GherkinPreview.vue'
import { useGherkinPreview } from '../composables/useGherkinPreview'

const store = useFeatureStore()
const activeTab = ref<'overview' | 'scenarios' | 'examples' | 'preview'>('overview')
const { render } = useGherkinPreview()

onMounted(() => {
  store.loadFeatures()
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

const preview = computed(() => render(store.selectedFeature))

function open(id: string) {
  store.openFeature(id)
}
</script>

<template>
  <div class="layout">
    <FeatureSidebar :items="store.features" :selected-id="store.selectedFeature?.id" :loading="store.loading" @select="open" />
    <main class="main">
      <div class="topbar">
        <input v-model="store.query" placeholder="Buscar por nombre, tag, ruta..." @input="store.loadFeatures" />
        <button @click="store.saveFeature" :disabled="!store.selectedFeature">Guardar</button>
      </div>

      <MetricCards :metrics="store.metrics" />

      <div v-if="store.selectedFeature" class="panel">
        <div class="tabs">
          <button @click="activeTab = 'overview'">Overview</button>
          <button @click="activeTab = 'scenarios'">Scenarios</button>
          <button @click="activeTab = 'examples'">Examples</button>
          <button @click="activeTab = 'preview'">Gherkin Preview</button>
        </div>

        <section v-if="activeTab === 'overview'">
          <input v-model="store.selectedFeature.name" class="feature-title" />
          <textarea v-model="store.selectedFeature.description" rows="3"></textarea>
          <p class="muted">Ruta: {{ store.selectedFeature.filePath }}</p>
          <div class="validation" v-if="store.selectedFeature.validation.errors.length">
            <strong>Errores:</strong>
            <ul><li v-for="err in store.selectedFeature.validation.errors" :key="err">{{ err }}</li></ul>
          </div>
        </section>

        <section v-if="activeTab === 'scenarios'">
          <ScenarioEditor v-for="scenario in store.selectedFeature.scenarios" :key="scenario.id" :scenario="scenario" />
        </section>

        <section v-if="activeTab === 'examples'">
          <ScenarioEditor
            v-for="scenario in store.selectedFeature.scenarios.filter((s) => s.type === 'OUTLINE')"
            :key="scenario.id"
            :scenario="scenario"
          />
        </section>

        <section v-if="activeTab === 'preview'">
          <GherkinPreview :content="preview" />
        </section>
      </div>
      <div v-else class="empty">Selecciona una feature para empezar.</div>
    </main>
  </div>
</template>
