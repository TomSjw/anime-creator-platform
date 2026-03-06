<template>
  <el-card>
    <h2>动漫自动生成</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧输入面板 -->
      <el-col :span="12">
        <el-form :model="form" label-width="100px">
          <el-form-item label="提示词">
            <el-input v-model="form.prompt" type="textarea" :rows="4" placeholder="请输入你想要生成的内容描述，例如：一个可爱的少女在樱花树下"></el-input>
          </el-form-item>
          <el-form-item label="动漫风格">
            <el-select v-model="form.style" placeholder="请选择风格" style="width: 100%;">
              <el-option label="日系动漫" value="Japanese anime" />
              <el-option label="国漫风格" value="Chinese animation" />
              <el-option label="赛博朋克" value="Cyberpunk" />
              <el-option label="治愈系" value="Healing style" />
              <el-option label="像素风" value="Pixel art" />
            </el-select>
          </el-form-item>
          <el-form-item label="分辨率">
            <el-select v-model="form.resolution" placeholder="请选择分辨率" style="width: 100%;">
              <el-option label="512×512" value="512x512" />
              <el-option label="768×768" value="768x768" />
              <el-option label="1024×1024" value="1024x1024" />
            </el-select>
          </el-form-item>
          <el-form-item label="AI工具">
            <el-select v-model="form.toolId" style="width: 100%;">
              <el-option label="Stable Diffusion（本地）" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generate" :loading="generating" :disabled="!form.prompt">
              开始生成
            </el-button>
          </el-form-item>
        </el-form>
      </el-col>

      <!-- 右侧进度与预览 -->
      <el-col :span="12">
        <div v-if="taskId" style="margin-bottom: 20px;">
          <el-progress :percentage="progress" :status="progressStatus" />
          <p style="margin-top: 8px; color: #606266;">
            状态：<el-tag :type="statusTagType">{{ statusLabel }}</el-tag>
          </p>
        </div>

        <div v-if="resultId" class="preview-area">
          <h4 style="margin-top: 0;">生成结果</h4>
          <img :src="'/api/result/file/' + resultId" style="max-width: 100%; border-radius: 4px;" />
          <div style="margin-top: 12px; display: flex; gap: 10px;">
            <el-button type="success" @click="goPublish">发布到小红书</el-button>
            <el-button @click="goResult">查看成果管理</el-button>
          </div>
        </div>

        <div v-else-if="!taskId" class="empty-preview">
          <p>输入需求后点击生成，即可在这里查看结果</p>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
// ✅ 修复：移除所有 this.$axios，改为直接 import axios
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { axios } from '../main.js'

const router = useRouter()

const form = ref({
  prompt: '',
  style: 'Japanese anime',
  resolution: '512x512',
  toolId: 1
})

const generating = ref(false)
const taskId = ref(null)
const progress = ref(0)
const taskStatus = ref('')
const resultId = ref(null)
let timer = null

// ✅ 状态展示计算属性，逻辑集中管理
const progressStatus = computed(() => {
  if (taskStatus.value === 'SUCCESS') return 'success'
  if (taskStatus.value === 'FAILED') return 'exception'
  return ''
})

const statusTagType = computed(() => {
  const map = { PENDING: 'warning', RUNNING: 'primary', SUCCESS: 'success', FAILED: 'danger' }
  return map[taskStatus.value] || 'info'
})

const statusLabel = computed(() => {
  const map = { PENDING: '等待中', RUNNING: '生成中', SUCCESS: '已完成', FAILED: '生成失败' }
  return map[taskStatus.value] || taskStatus.value
})

const generate = async () => {
  generating.value = true
  taskId.value = null
  resultId.value = null
  progress.value = 0
  taskStatus.value = ''

  try {
    const [width, height] = form.value.resolution.split('x').map(Number)
    const res = await axios.post('/task/generate', {
      prompt: form.value.prompt,
      style: form.value.style,
      width,
      height,
      toolId: form.value.toolId
    })
    taskId.value = res.data.data
    ElMessage.success('任务已创建，正在生成...')
    timer = setInterval(pollTask, 1500)
  } catch (e) {
    ElMessage.error('创建任务失败，请检查后端服务是否启动')
    generating.value = false
  }
}

const pollTask = async () => {
  if (!taskId.value) return
  try {
    const res = await axios.get('/task/' + taskId.value)
    const task = res.data.data
    progress.value = task.progress || 0
    taskStatus.value = task.status

    if (task.status === 'SUCCESS') {
      resultId.value = task.resultId
      clearInterval(timer)
      generating.value = false
      ElMessage.success('生成完成！')
    } else if (task.status === 'FAILED') {
      clearInterval(timer)
      generating.value = false
      ElMessage.error('生成失败：' + (task.errorMsg || '请检查 SD WebUI 是否已开启 --api 模式'))
    }
  } catch (e) {
    console.error('轮询任务状态失败', e)
  }
}

const goPublish = () => router.push('/publish?resultId=' + resultId.value)
const goResult = () => router.push('/result')

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.empty-preview {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
}
.preview-area {
  border: 1px solid #eee;
  padding: 16px;
  border-radius: 8px;
  background: #fff;
}
</style>
