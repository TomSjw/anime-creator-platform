<template>
  <el-container style="height: 100vh;">
    <el-aside width="200px" style="background-color: rgb(247, 249, 251);">
      <el-menu :default-active="activeIndex" router>
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/generate">
          <el-icon><Picture /></el-icon>
          <span>动漫生成</span>
        </el-menu-item>
        <el-menu-item index="/result">
          <el-icon><FolderOpened /></el-icon>
          <span>成果管理</span>
        </el-menu-item>
        <el-menu-item index="/task">
          <el-icon><Clock /></el-icon>
          <span>任务监控</span>
        </el-menu-item>
        <el-menu-item index="/setting">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="display:flex; align-items:center; justify-content:space-between; border-bottom: 1px solid #eee;">
        <span style="font-weight: bold;">私人动漫创作自动化工作平台</span>
        <el-button link @click="logout">退出登录</el-button>
      </el-header>
      <el-main style="background-color: #f5f7fa;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// ✅ 修复：Vue3 script setup 无 this，直接 import axios 和 router
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Picture, FolderOpened, Clock, Setting } from '@element-plus/icons-vue'
import { axios } from './main.js'

const router = useRouter()
const route = useRoute()
const activeIndex = ref(route.path)

watch(() => route.path, (newPath) => {
  activeIndex.value = newPath
})

const logout = async () => {
  try {
    await axios.post('/auth/logout')
  } catch (e) { /* 忽略错误，强制跳转 */ }
  localStorage.removeItem('isLoggedIn')  // ✅ 清除登录标记
  router.push('/login')
}
</script>

<style>
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
}
</style>
