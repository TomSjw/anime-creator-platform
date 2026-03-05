<template>
  <el-card>
    <h2>动漫自动生成</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧输入面板 -->
      <el-col :span="12">
        <el-form :model="form" label-width="100px">
          <el-form-item label="提示词">
            <el-input v-model="form.prompt" type="textarea" :rows="4" placeholder="请输入你想要生成的内容描述"></el-input>
          </el-form-item>
          <el-form-item label="动漫风格">
            <el-select v-model="form.style" placeholder="请选择风格">
              <el-option label="日系动漫" value="Japanese anime"></el-option>
              <el-option label="国漫风格" value="Chinese animation"></el-option>
              <el-option label="赛博朋克" value="Cyberpunk"></el-option>
              <el-option label="治愈系" value="Healing style"></el-option>
              <el-option label="像素风" value="Pixel art"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="分辨率">
            <el-select v-model="form.resolution" placeholder="请选择分辨率">
              <el-option label="512x512" value="512x512"></el-option>
              <el-option label="768x768" value="768x768"></el-option>
              <el-option label="1024x1024" value="1024x1024"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="AI工具">
            <el-select v-model="form.toolId" placeholder="请选择AI工具">
              <el-option label="Stable Diffusion (本地)" :value="1"></el-option>
              <!-- 后续可动态加载配置的工具 -->
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generate" :loading="generating" :disabled="!form.prompt">开始生成</el-button>
          </el-form-item>
        </el-form>
      </el-col>
      <!-- 右侧进度与预览 -->
      <el-col :span="12">
        <div v-if="taskId">
          <el-progress :percentage="progress" :status="progressStatus"></el-progress>
          <p style="margin-top: 10px;">任务状态: {{ taskStatus }}</p>
        </div>
        <div v-if="resultId" class="preview-area">
          <h4>生成结果</h4>
          <img :src="'/api/result/file/' + resultId" style="max-width: 100%; border: 1px solid #eee;" />
          <div style="margin-top: 10px;">
            <el-button type="success" @click="goPublish">直接发布小红书</el-button>
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
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
const progressStatus = ref('')
const taskStatus = ref('')
const resultId = ref(null)
let timer = null

const generate = async () => {
  generating.value = true
  try {
    const [width, height] = form.value.resolution.split('x').map(Number)
    const param = {
      prompt: form.value.prompt,
      style: form.value.style,
      width,
      height,
      toolId: form.value.toolId
    }
    const res = await this.$axios.post('/task/generate', param)
    taskId.value = res.data.data
    ElMessage.success('任务已创建，开始生成...')
    
    // 开始轮询任务状态
    timer = setInterval(pollTask, 1000)
  } catch(e) {
    ElMessage.error('创建任务失败')
    generating.value = false
  }
}

const pollTask = async () => {
  if (!taskId.value) return
  try {
    const res = await this.$axios.get('/task/' + taskId.value)
    const task = res.data.data
    progress.value = task.progress
    taskStatus.value = task.status
    if (task.status === 'RUNNING') {
      progressStatus.value = 'info'
    } else if (task.status === 'SUCCESS') {
      progressStatus.value = 'success'
      resultId.value = task.resultId
      clearInterval(timer)
      generating.value = false
      ElMessage.success('生成完成！')
    } else if (task.status === 'FAILED') {
      progressStatus.value = 'exception'
      clearInterval(timer)
      generating.value = false
      ElMessage.error('生成失败: ' + task.errorMsg)
    }
  } catch(e) {
    console.error(e)
  }
}

const goPublish = () => {
  router.push('/publish?resultId=' + resultId.value)
}

const goResult = () => {
  router.push('/result')
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.empty-preview {
  text-align: center;
  padding: 100px 0;
  color: #909399;
  border: 1px dashed #dcdfe6;
}
.preview-area {
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 4px;
}
</style>
