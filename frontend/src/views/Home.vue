<template>
  <div>
    <h2>欢迎回来，开始你的动漫创作吧！</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">今日生成</div>
            <div style="font-size: 36px; font-weight: bold; color: #409eff; margin-top: 8px;">{{ stats.todayGenerate }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">今日发布</div>
            <div style="font-size: 36px; font-weight: bold; color: #67c23a; margin-top: 8px;">{{ stats.todayPublish }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">待处理任务</div>
            <div style="font-size: 36px; font-weight: bold; color: #e6a23c; margin-top: 8px;">{{ stats.pendingTask }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card header="快速入口">
          <div style="display: flex; flex-wrap: wrap; gap: 10px;">
            <el-button type="primary" @click="router.push('/generate')">动漫生成</el-button>
            <el-button type="success" @click="router.push('/result')">成果管理</el-button>
            <el-button type="warning" @click="router.push('/task')">任务监控</el-button>
            <el-button type="info" @click="router.push('/setting')">系统设置</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="最近任务">
          <el-table :data="recentTasks" border size="small">
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// ✅ 修复：移除 this.$axios
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { axios } from '../main.js'

const router = useRouter()
const stats = ref({ todayGenerate: 0, todayPublish: 0, pendingTask: 0 })
const recentTasks = ref([])

const statusTagType = (status) => {
  return { PENDING: 'warning', RUNNING: 'primary', SUCCESS: 'success', FAILED: 'danger' }[status] || 'info'
}

onMounted(async () => {
  try {
    const res = await axios.get('/task/list', { params: { page: 1, size: 5 } })
    const records = res.data.data?.records || []
    recentTasks.value = records

    // 简单统计
    const today = new Date().toDateString()
    stats.value.todayGenerate = records.filter(t =>
      t.taskType === 'GENERATE' && new Date(t.createTime).toDateString() === today
    ).length
    stats.value.todayPublish = records.filter(t =>
      t.taskType === 'PUBLISH' && t.status === 'SUCCESS' && new Date(t.createTime).toDateString() === today
    ).length
    stats.value.pendingTask = records.filter(t => t.status === 'PENDING' || t.status === 'RUNNING').length
  } catch (e) {
    console.error('加载首页数据失败', e)
  }
})
</script>
