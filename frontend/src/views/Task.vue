<template>
  <el-card>
    <h2>任务监控</h2>
    <el-tabs v-model="activeTab" @tab-change="onTabChange" style="margin-top: 10px;">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="等待中" name="PENDING" />
      <el-tab-pane label="运行中" name="RUNNING" />
      <el-tab-pane label="已成功" name="SUCCESS" />
      <el-tab-pane label="已失败" name="FAILED" />
    </el-tabs>

    <el-table :data="list" border style="width: 100%;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="taskName" label="任务名称" />
      <el-table-column prop="taskType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.taskType === 'GENERATE' ? 'primary' : 'warning'">
            {{ row.taskType === 'GENERATE' ? '生成' : '发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="120">
        <template #default="{ row }">
          <el-progress :percentage="row.progress || 0" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="warning" @click="retry(row)" v-if="row.status === 'FAILED'">重试</el-button>
          <el-button size="small" type="danger" @click="deleteTask(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end; display: flex;"
      @change="loadData"
    />

    <el-dialog title="任务详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务ID">{{ detail?.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detail?.taskName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail?.taskType === 'GENERATE' ? '生成任务' : '发布任务' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail?.status)">{{ statusLabel(detail?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="任务参数">
          <pre style="margin:0; font-size:12px; white-space: pre-wrap;">{{ detail?.param }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="任务结果" v-if="detail?.result">{{ detail.result }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="detail?.errorMsg">
          <span style="color: #f56c6c;">{{ detail.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
// ✅ 修复：移除 this.$axios
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { axios } from '../main.js'

const activeTab = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref([])
const detailVisible = ref(false)
const detail = ref(null)
let autoRefreshTimer = null

const statusTagType = (s) => ({ PENDING: 'warning', RUNNING: 'primary', SUCCESS: 'success', FAILED: 'danger' }[s] || 'info')
const statusLabel = (s) => ({ PENDING: '等待中', RUNNING: '运行中', SUCCESS: '已成功', FAILED: '已失败' }[s] || s)
const formatTime = (t) => t ? String(t).substring(0, 16).replace('T', ' ') : ''

const loadData = async () => {
  try {
    const res = await axios.get('/task/list', {
      params: { page: page.value, size: size.value, status: activeTab.value }
    })
    list.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } catch (e) {
    console.error('加载任务列表失败', e)
  }
}

const onTabChange = () => { page.value = 1; loadData() }

const viewDetail = (row) => { detail.value = row; detailVisible.value = true }

const retry = async (row) => {
  try {
    await axios.post('/task/retry/' + row.id)
    ElMessage.success('重试任务已提交')
    loadData()
  } catch (e) {
    ElMessage.error('重试失败')
  }
}

const deleteTask = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该任务吗？', '提示', { type: 'warning' })
    await axios.delete('/task/' + row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* 用户取消 */ }
}

onMounted(() => {
  loadData()
  // ✅ 每 3 秒自动刷新，方便监控运行中任务
  autoRefreshTimer = setInterval(loadData, 3000)
})

onUnmounted(() => {
  // ✅ 修复：组件销毁时清除定时器，避免内存泄漏
  if (autoRefreshTimer) clearInterval(autoRefreshTimer)
})
</script>
