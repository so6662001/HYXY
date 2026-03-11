<template>
  <div class="score-progress">
    <div class="score-header">
      <span class="score-name">{{ name }}</span>
      <span class="score-value">{{ percentage }}%</span>
    </div>
    <div class="progress-bar">
      <div
        class="progress-fill"
        :style="{ width: percentage + '%', background: barColor }"
      />
    </div>
    <div class="score-footer">
      <span>{{ score }} / {{ maxScore }}</span>
      <span class="score-level">{{ level }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getProgressColor } from '../../utils/credit-helpers'

const props = defineProps<{
  name: string
  score: number
  maxScore: number
  percentage: number
  level: string
}>()

const barColor = computed(() => getProgressColor(props.percentage))
</script>

<style scoped>
.score-progress {
  margin-bottom: 14px;
}

.score-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.score-name {
  font-size: 14px;
  font-weight: 500;
}

.score-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--credit-primary);
}

.score-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.score-level {
  font-weight: 500;
}
</style>
