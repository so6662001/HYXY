<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
          <span class="logo-text">钢铁交易平台 · 企业信用系统</span>
        </div>

        <!-- Desktop menu -->
        <el-menu
          v-if="!isMobile"
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
        >
          <el-menu-item index="/inquiry">企业信用</el-menu-item>
        </el-menu>

        <!-- Mobile hamburger -->
        <el-icon
          v-if="isMobile"
          class="mobile-menu-trigger"
          :size="24"
          @click="drawerVisible = true"
        >
          <Menu />
        </el-icon>
      </div>
    </el-header>

    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="70%"
      :with-header="false"
    >
      <el-menu
        :default-active="activeMenu"
        router
        @select="drawerVisible = false"
        class="mobile-nav-menu"
      >
        <el-menu-item index="/inquiry">企业信用</el-menu-item>
      </el-menu>
    </el-drawer>

    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const drawerVisible = ref(false)
const windowWidth = ref(window.innerWidth)

function onResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))

const isMobile = computed(() => windowWidth.value < 768)
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 0;
  min-height: 56px;
  height: auto;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--credit-primary);
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.header-menu {
  border-bottom: none;
}

.mobile-menu-trigger {
  cursor: pointer;
  color: var(--text-primary);
  padding: 8px;
}

.mobile-nav-menu {
  border-right: none;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }
}

@media (max-width: 480px) {
  .logo-text {
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 200px;
  }
}

@media (max-width: 375px) {
  .logo-text {
    max-width: 160px;
  }
}
</style>
