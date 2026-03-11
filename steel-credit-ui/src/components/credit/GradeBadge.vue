<template>
  <div class="grade-badge" :class="`grade-${grade}`">
    {{ grade === '暂无' ? 'N/A' : grade }}
  </div>
</template>

<script setup lang="ts">
defineProps<{ grade: string }>()
</script>
