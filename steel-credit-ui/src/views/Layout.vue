<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
          <span class="logo-text">钢铁交易平台 · 企业信用系统</span>
        </div>
        <el-menu mode="horizontal" :default-active="activeMenu" router class="header-menu">
          <el-menu-item index="/enterprise/1001">企业信用</el-menu-item>
          <el-menu-item index="/inquiry">询报价列表</el-menu-item>
        </el-menu>
      </div>
    </el-header>
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 0;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 40px;
  color: var(--credit-primary);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.header-menu {
  border-bottom: none;
}

.layout-main {
  background: var(--bg-page);
  padding: 0;
}
</style>
