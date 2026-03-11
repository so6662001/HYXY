<template>
  <div class="sufficiency-wrapper">
    <div class="sufficiency-dots">
      <div
        v-for="(filled, idx) in blocks"
        :key="idx"
        class="sufficiency-dot"
        :class="{ filled }"
      />
    </div>
    <span class="sufficiency-label">{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getSufficiencyBlocks } from '../../utils/credit-helpers'

const props = defineProps<{
  level: number
  label: string
}>()

const blocks = computed(() => getSufficiencyBlocks(props.level))
</script>

<style scoped>
.sufficiency-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.sufficiency-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
