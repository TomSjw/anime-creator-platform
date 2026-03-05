<template>
  <div>
    <h2>欢迎回来，开始你的动漫创作吧！</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">今日生成</div>
            <div style="font-size: 30px; font-weight: bold; color: #409eff; margin-top: 10px;">{{ stats.todayGenerate }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">今日发布</div>
            <div style="font-size: 30px; font-weight: bold; color: #67c23a; margin-top: 10px;">{{ stats.todayPublish }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">待处理任务</div>
            <div style="font-size: 30px; font-weight: bold; color: #e6a23c; margin-top: 10px;">{{ stats.pendingTask }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card title="快速入口">
          <el-button type="primary" size="large" @click="goTo('/generate')">动漫生成</el-button>
          <el-button type="success" size="large" @click="goTo('/result')">成果管理</el-button>
          <el-button type="warning" size="large" @click="goTo('/task')">任务监控</el-button>
          <el-button type="info" size="large" @click="goTo('/setting')">系统设置</el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="最近任务">
          <el-table :data="recentTasks" border>
            <el-table-column prop="taskName" label="任务名称"></el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = ref({
  todayGenerate: 0,
  todayPublish: 0,
  pendingTask: 0
})
const recentTasks = ref([])

const goTo = (path) => {
  router.push(path)
}

const getStatusTag = (status) => {
  const map = {
    'PENDING': 'warning',
    'RUNNING': 'info',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return map[status] || 'info'
}

const loadData = async () => {
  try {
    // TODO: 统计接口，这里先模拟，后续可以扩展
    // 加载最近任务
    const res = await this.$axios.get('/task/list?page=1&size=5')
    recentTasks.value = res.data.data.records
  } catch(e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>
