<template>
  <el-card>
    <h2>小红书发布</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧成果预览 -->
      <el-col :span="12">
        <h4>成果预览</h4>
        <img v-if="result" :src="'/api/result/file/' + result.id" style="max-width: 100%; border: 1px solid #eee;" />
      </el-col>
      <!-- 右侧发布参数 -->
      <el-col :span="12">
        <el-form :model="form" label-width="100px">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="请输入笔记标题"></el-input>
          </el-form-item>
          <el-form-item label="文案">
            <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入笔记文案"></el-input>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tags" multiple placeholder="请选择标签">
              <el-option label="#AI动漫" value="#AI动漫"></el-option>
              <el-option label="#动漫插画" value="#动漫插画"></el-option>
              <el-option label="#日系动漫" value="#日系动漫"></el-option>
              <el-option label="#AI绘画" value="#AI绘画"></el-option>
              <el-option label="#原创插画" value="#原创插画"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="doPublish" :loading="publishing">确认发布</el-button>
            <el-button @click="back">返回</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const resultId = ref(route.query.resultId)
const result = ref(null)
const publishing = ref(false)
const form = ref({
  title: '',
  content: '今日动漫生成✨，用AI画的治愈系插画~',
  tags: ['#AI动漫', '#动漫插画']
})

const loadResult = async () => {
  try {
    const res = await this.$axios.get('/result/' + resultId.value)
    result.value = res.data.data
    // 默认标题用提示词
    if (!form.value.title) {
      form.value.title = result.value.prompt.substring(0, 20)
    }
  } catch(e) {
    ElMessage.error('加载成果失败')
  }
}

const doPublish = async () => {
  if (!form.value.title) {
    ElMessage.error('请输入标题')
    return
  }
  publishing.value = true
  try {
    const param = {
      resultId: resultId.value,
      title: form.value.title,
      content: form.value.content,
      tags: form.value.tags.join(',')
    }
    await this.$axios.post('/task/publish', param)
    ElMessage.success('发布任务已创建，正在后台执行...')
    router.push('/task')
  } catch(e) {
    ElMessage.error('创建发布任务失败')
  } finally {
    publishing.value = false
  }
}

const back = () => {
  router.back()
}

onMounted(() => {
  loadResult()
})
</script>
