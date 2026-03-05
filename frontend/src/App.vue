<template>
  <el-container style="height: 100vh; border: 1px solid #eee;">
    <el-aside width="200px" style="background-color: rgb(247, 249, 251);">
      <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose" router>
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
      <el-header style="text-align: right; font-size: 12px;">
        <span>私人动漫创作自动化工作平台</span>
        <el-button type="text" @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { House, Picture, FolderOpened, Clock, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const activeIndex = ref(router.currentRoute.value.path)

onMounted(() => {
  router.afterEach((to) => {
    activeIndex.value = to.path
  })
})

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  console.log(key, keyPath)
}

const logout = async () => {
  try {
    await this.$axios.post('/auth/logout')
  } catch(e) {}
  router.push('/login')
}
</script>

<style>
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  border-bottom: 1px solid #eee;
}
.el-aside {
  color: #333;
}
.el-main {
  background-color: #f5f7fa;
}
</style>
