<template>
  <el-card>
    <h2>任务监控</h2>
    <!-- 状态筛选 -->
    <el-tabs v-model="activeTab" style="margin-top: 20px;" @tab-change="loadData">
      <el-tab-pane label="全部" name=""></el-tab-pane>
      <el-tab-pane label="待执行" name="PENDING"></el-tab-pane>
      <el-tab-pane label="运行中" name="RUNNING"></el-tab-pane>
      <el-tab-pane label="已成功" name="SUCCESS"></el-tab-pane>
      <el-tab-pane label="已失败" name="FAILED"></el-tab-pane>
    </el-tabs>

    <!-- 任务列表 -->
    <el-table :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="任务ID" width="100"></el-table-column>
      <el-table-column prop="taskName" label="任务名称"></el-table-column>
      <el-table-column prop="taskType" label="任务类型" width="100">
        <template #default="scope">
          {{ scope.row.taskType === 'GENERATE' ? '生成任务' : '发布任务' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="100">
        <template #default="scope">
          <el-progress :percentage="scope.row.progress" :show-text="true" :stroke-width="8"></el-progress>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button size="small" type="warning" @click="retry(scope.row)" v-if="scope.row.status === 'FAILED'">重试</el-button>
          <el-button size="small" type="danger" @click="deleteTask(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      style="margin-top: 20px; text-align: right;"
      @change="loadData"
    />

    <!-- 详情弹窗 -->
    <el-dialog title="任务详情" v-model="detailDialogVisible" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务ID">{{ detailTask?.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailTask?.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ detailTask?.taskType === 'GENERATE' ? '生成任务' : '发布任务' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailTask?.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailTask?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="任务参数">
          <pre>{{ detailTask?.param }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="任务结果" v-if="detailTask?.result">
          <pre>{{ detailTask?.result }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="detailTask?.errorMsg" style="color: #f56c6c;">
          {{ detailTask?.errorMsg }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref([])
const detailDialogVisible = ref(false)
const detailTask = ref(null)

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
    const params = {
      page: page.value,
      size: size.value,
      status: activeTab.value
    }
    const res = await this.$axios.get('/task/list', { params })
    list.value = res.data.data.records
    total.value = res.data.data.total
  } catch(e) {
    console.error(e)
  }
}

const viewDetail = (row) => {
  detailTask.value = row
  detailDialogVisible.value = true
}

const retry = async (row) => {
  try {
    await this.$axios.post('/task/retry/' + row.id)
    ElMessage.success('重试成功')
    loadData()
  } catch(e) {
    ElMessage.error('重试失败')
  }
}

const deleteTask = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该任务吗？', '提示')
    await this.$axios.delete('/task/' + row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch(e) {}
}

onMounted(() => {
  loadData()
  // 自动刷新
  setInterval(loadData, 3000)
})
</script>
