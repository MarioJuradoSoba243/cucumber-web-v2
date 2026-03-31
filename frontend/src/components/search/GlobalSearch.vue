<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { api } from '../../services/api'
import type { SearchPage, SearchType } from '../../types/feature'

const emit = defineEmits<{ (e: 'select', payload: { featureId: string; field: string }): void }>()
const query = ref('')
const types = ref<SearchType[]>([])
const tag = ref('')
const page = ref(0)
const result = ref<SearchPage>({ results: [], total: 0, page: 0, size: 10 })
const loading = ref(false)
let timer: number | undefined

async function runSearch() {
  if (!query.value.trim()) {
    result.value = { results: [], total: 0, page: 0, size: 10 }
    return
  }
  loading.value = true
  result.value = await api.search({ q: query.value, types: types.value, tags: tag.value ? [tag.value] : [], page: page.value, size: 10 })
  loading.value = false
}

watch([query, types, tag, page], () => {
  clearTimeout(timer)
  timer = window.setTimeout(runSearch, 300)
})

function onHotKey(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    const input = document.getElementById('global-search-input') as HTMLInputElement | null
    input?.focus()
  }
}

onMounted(() => window.addEventListener('keydown', onHotKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onHotKey))
</script>

<template>
  <div class="card section-card">
    <h3>Búsqueda global</h3>
    <input id="global-search-input" v-model="query" placeholder="Buscar en features, scenarios, steps, examples..." />
    <p class="muted">Atajo: Ctrl/Cmd + K</p>
    <div v-if="loading">Buscando...</div>
    <div v-else-if="!result.results.length" class="empty">Sin resultados</div>
    <div v-else>
      <article v-for="feature in result.results" :key="feature.featureId" class="card">
        <h4>{{ feature.featureName }}</h4>
        <small>{{ feature.filePath }}</small>
        <ul>
          <li v-for="hit in feature.hits" :key="`${hit.location.field}-${hit.text}`">
            <button class="ghost" @click="emit('select', { featureId: feature.featureId, field: hit.location.field })">
              <span v-html="hit.highlight" />
            </button>
          </li>
        </ul>
      </article>
    </div>
  </div>
</template>
