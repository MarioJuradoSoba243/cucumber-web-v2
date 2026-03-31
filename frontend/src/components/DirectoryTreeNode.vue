<script setup lang="ts">
import { computed, ref } from 'vue'
import type { DirectoryNode } from '../types/feature'

const props = defineProps<{
  node: DirectoryNode
  selectedId?: string
  level?: number
  rootPath: string
}>()

const emit = defineEmits<{
  select: [id: string]
  createFeature: [folderPath: string]
  createFolder: [folderPath: string]
  renamePath: [path: string]
  movePath: [sourcePath: string]
}>()

const expanded = ref(true)
const depth = computed(() => props.level ?? 0)
const indent = computed(() => Math.min(depth.value * 10, 64))
const featureIndent = computed(() => Math.min((depth.value + 1) * 10, 74))
const currentPath = computed(() => props.node.path)
const nodeLabel = computed(() => {
  if (!currentPath.value) return 'Raíz'
  const parts = currentPath.value.split('/').filter(Boolean)
  return parts.at(-1) ?? currentPath.value
})
</script>

<template>
  <div class="tree-node">
    <div class="tree-folder" :style="{ paddingLeft: `${indent}px` }">
      <button class="ghost icon-btn tiny" @click="expanded = !expanded">{{ expanded ? '▾' : '▸' }}</button>
      <button class="folder-label" :title="currentPath || props.rootPath" @click="emit('createFeature', currentPath)">📁 {{ nodeLabel }}</button>
      <div class="node-actions">
        <button class="ghost tiny icon-only" title="Nueva carpeta" @click="emit('createFolder', currentPath)">📁➕</button>
        <button v-if="currentPath" class="ghost tiny icon-only" title="Renombrar" @click="emit('renamePath', currentPath)">✏️</button>
        <button v-if="currentPath" class="ghost tiny icon-only" title="Mover" @click="emit('movePath', currentPath)">↔️</button>
      </div>
    </div>

    <div v-if="expanded">
      <button
        v-for="feature in node.features"
        :key="feature.id"
        :class="['feature-item tree-feature', { active: selectedId === feature.id }]"
        :style="{ marginLeft: `${featureIndent}px`, width: `calc(100% - ${featureIndent}px)` }"
        @click="emit('select', feature.id)"
      >
        <div class="feature-row">
          <div class="feature-item-title" :title="feature.name">🧪 {{ feature.name }}</div>
          <div class="row-actions">
            <button class="ghost tiny icon-only" title="Renombrar feature" type="button" @click.stop="emit('renamePath', feature.id)">✏️</button>
            <button class="ghost tiny icon-only" title="Mover feature" type="button" @click.stop="emit('movePath', feature.id)">↔️</button>
          </div>
        </div>
      </button>

      <DirectoryTreeNode
        v-for="folder in node.folders"
        :key="folder.id"
        :node="folder"
        :selected-id="selectedId"
        :level="depth + 1"
        :root-path="rootPath"
        @select="(id) => emit('select', id)"
        @create-feature="(path) => emit('createFeature', path)"
        @create-folder="(path) => emit('createFolder', path)"
        @rename-path="(path) => emit('renamePath', path)"
        @move-path="(path) => emit('movePath', path)"
      />
    </div>
  </div>
</template>
