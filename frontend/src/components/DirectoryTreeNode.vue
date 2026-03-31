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
const currentPath = computed(() => props.node.path)
const nodeLabel = computed(() => currentPath.value || props.rootPath)
</script>

<template>
  <div class="tree-node">
    <div class="tree-folder" :style="{ paddingLeft: `${depth * 14}px` }">
      <button class="ghost icon-btn tiny" @click="expanded = !expanded">{{ expanded ? '▾' : '▸' }}</button>
      <button class="folder-label" @click="emit('createFeature', currentPath)">📁 {{ nodeLabel }}</button>
      <div class="node-actions">
        <button class="ghost tiny" @click="emit('createFolder', currentPath)">+ dir</button>
        <button v-if="currentPath" class="ghost tiny" @click="emit('renamePath', currentPath)">Renombrar</button>
        <button v-if="currentPath" class="ghost tiny" @click="emit('movePath', currentPath)">Mover</button>
      </div>
    </div>

    <div v-if="expanded">
      <button
        v-for="feature in node.features"
        :key="feature.id"
        :class="['feature-item tree-feature', { active: selectedId === feature.id }]"
        :style="{ marginLeft: `${(depth + 1) * 14}px` }"
        @click="emit('select', feature.id)"
      >
        <div class="feature-item-title">🧪 {{ feature.name }}</div>
        <div class="meta">{{ feature.scenarioCount }} escenarios</div>
        <div class="row-actions">
          <button class="ghost tiny" type="button" @click.stop="emit('renamePath', feature.id)">Renombrar</button>
          <button class="ghost tiny" type="button" @click.stop="emit('movePath', feature.id)">Mover</button>
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
