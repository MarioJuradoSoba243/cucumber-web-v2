<script setup lang="ts">
import { ref } from 'vue'
import type { DirectoryNode } from '../types/feature'
import DirectoryTreeNode from './DirectoryTreeNode.vue'

defineProps<{
  tree: DirectoryNode | null
  selectedId?: string
  loading: boolean
  query: string
  collapsed: boolean
  rootPath: string
}>()

const emit = defineEmits<{
  select: [id: string]
  search: [value: string]
  create: [name: string, folderPath: string]
  createFolder: [parentPath: string, name: string]
  renamePath: [path: string, newName: string]
  movePath: [sourcePath: string, destinationPath: string]
  toggle: []
}>()

const featureName = ref('')
const selectedFolderPath = ref('')


function createFeature() {
  emit('create', featureName.value, selectedFolderPath.value)
  featureName.value = ''
}

function setCreateFolderPath(path: string) {
  selectedFolderPath.value = path
}

function askCreateFolder(parentPath: string) {
  const name = window.prompt('Nombre de la nueva carpeta')
  if (name?.trim()) {
    emit('createFolder', parentPath, name.trim())
  }
}

function askRename(path: string) {
  const newName = window.prompt('Nuevo nombre', path.split('/').pop() ?? '')
  if (newName?.trim()) {
    emit('renamePath', path, newName.trim())
  }
}

function askMove(sourcePath: string) {
  const destination = window.prompt('Mover a carpeta (ruta relativa, vacío para raíz)', '')
  if (destination !== null) {
    emit('movePath', sourcePath, destination.trim())
  }
}
</script>

<template>
  <aside :class="['sidebar', { collapsed }]">
      <div class="sidebar-header">
      <button class="ghost icon-btn" title="Contraer barra lateral" @click="emit('toggle')">{{ collapsed ? '☰' : '⟨' }}</button>
      <div v-if="!collapsed" class="brand">
        <h2>Cucumber Studio</h2>
        <span>QA Workspace</span>
      </div>
    </div>

    <template v-if="!collapsed">
      <label class="search-wrap">
        <span>🔎</span>
        <input :value="query" placeholder="Buscar features" @input="emit('search', ($event.target as HTMLInputElement).value)" />
      </label>

      <div class="feature-list-header">
        <h3>Explorador</h3>
      </div>

      <div v-if="loading" class="sidebar-state">Cargando features...</div>
      <div v-else-if="!tree" class="sidebar-state">No hay estructura disponible.</div>

      <div class="feature-list">
        <DirectoryTreeNode
          v-if="tree"
          :node="tree"
          :selected-id="selectedId"
          :root-path="rootPath"
          @select="(id) => emit('select', id)"
          @create-feature="setCreateFolderPath"
          @create-folder="askCreateFolder"
          @rename-path="askRename"
          @move-path="askMove"
        />
      </div>

      <div class="sidebar-footer">
        <input v-model="featureName" placeholder="Nueva feature" @keyup.enter="createFeature" />
        <button class="primary icon-only" title="Crear feature" @click="createFeature">➕</button>
      </div>
    </template>
  </aside>
</template>
