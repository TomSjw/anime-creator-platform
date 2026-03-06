<template>
  <el-card>
    <h2>发布到小红书</h2>
    <el-row :gutter="24" style="margin-top: 20px;">
      <el-col :span="10">
        <h4 style="margin-top: 0;">成果预览</h4>
        <div v-if="result" style="border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
          <img :src="'/api/result/file/' + result.id" style="width: 100%; display: block;" />
          <div style="padding: 10px; font-size: 12px; color: #606266;">
            <p style="margin: 0;">提示词：{{ result.prompt }}</p>
            <p style="margin: 6px 0 0;">风格：{{ result.style }}</p>
          </div>
        </div>
        <el-skeleton v-else :rows="5" animated />
      </el-col>

      <el-col :span="14">
        <h4 style="margin-top: 0;">发布信息</h4>
        <el-form :model="form" label-width="80px">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="笔记标题（建议20字以内）" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="文案">
            <el-input v-model="form.content" type="textarea" :rows="5" placeholder="笔记文案" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tags" multiple placeholder="选择话题标签" style="width: 100%;">
              <el-option label="#AI动漫" value="#AI动漫" />
              <el-option label="#动漫插画" value="#动漫插画" />
              <el-option label="#日系动漫" value="#日系动漫" />
              <el-option label="#AI绘画" value="#AI绘画" />
              <el-option label="#原创插画" value="#原创插画" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="doPublish" :loading="publishing" :disabled="!form.title">
              确认发布
            </el-button>
            <el-button @click="router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
// ✅ 修复：移除 this.$axios
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { axios } from '../main.js'

const route = useRoute()
const router = useRouter()
const resultId = route.query.resultId
const result = ref(null)
const publishing = ref(false)

const form = ref({
  title: '',
  content: '今日动漫生成✨，用AI画的治愈系插画~',
  tags: ['#AI动漫', '#动漫插画']
})

onMounted(async () => {
  if (!resultId) {
    ElMessage.error('缺少成果ID，请从成果管理页面进入')
    router.push('/result')
    return
  }
  try {
    const res = await axios.get('/result/' + resultId)
    result.value = res.data.data
    if (result.value?.prompt && !form.value.title) {
      form.value.title = result.value.prompt.substring(0, 20)
    }
  } catch (e) {
    ElMessage.error('加载成果信息失败')
  }
})

const doPublish = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  publishing.value = true
  try {
    await axios.post('/task/publish', {
      resultId: Number(resultId),
      title: form.value.title,
      content: form.value.content,
      tags: form.value.tags.join(',')
    })
    ElMessage.success('发布任务已创建，正在后台执行...')
    router.push('/task')
  } catch (e) {
    ElMessage.error('创建发布任务失败')
  } finally {
    publishing.value = false
  }
}
</script>
