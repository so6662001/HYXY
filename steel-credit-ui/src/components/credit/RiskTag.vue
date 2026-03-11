<template>
  <el-tag
    :type="tagType"
    :effect="effect"
    round
    size="default"
    class="risk-tag"
  >
    {{ icon }} {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getRiskLabel } from '../../utils/credit-helpers'

const props = defineProps<{
  level: string
  effect?: 'dark' | 'light' | 'plain'
}>()

const label = computed(() => getRiskLabel(props.level))

const icon = computed(() => {
  const map: Record<string, string> = {
    HIGH: '🔴', MEDIUM: '🟠', LOW: '🟡', NONE: '🟢'
  }
  return map[props.level] || '⚪'
})

const tagType = computed(() => {
  const map: Record<string, 'danger' | 'warning' | 'success' | 'info'> = {
    HIGH: 'danger', MEDIUM: 'warning', LOW: 'warning', NONE: 'success'
  }
  return map[props.level] || 'info'
})
</script>

<style scoped>
.risk-tag {
  font-size: 13px;
}
</style>
