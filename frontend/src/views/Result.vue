<template>
  <el-card>
    <h2>成果管理</h2>
    <div style="margin: 20px 0; display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
      <el-select v-model="filter.fileType" placeholder="文件类型" clearable style="width: 120px;">
        <el-option label="图片" value="IMAGE" />
        <el-option label="视频" value="VIDEO" />
      </el-select>
      <el-select v-model="filter.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="待发布" value="PENDING" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="已废弃" value="DISCARDED" />
      </el-select>
      <el-input v-model="filter.keyword" placeholder="搜索提示词" clearable style="width: 200px;" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-empty v-if="list.length === 0" description="暂无成果，去生成一张吧！" />

    <el-row :gutter="16">
      <el-col :span="6" v-for="item in list" :key="item.id" style="margin-bottom: 16px;">
        <el-card class="result-card" :body-style="{ padding: '0' }">
          <img :src="'/api/result/file/' + item.id" class="result-img" @error="onImgError" />
          <div class="result-actions">
            <el-button size="small" @click="preview(item)">预览</el-button>
            <el-button size="small" type="success" @click="publish(item)" v-if="item.status === 'PENDING'">发布</el-button>
            <el-button size="small" @click="download(item)">下载</el-button>
            <el-button size="small" type="danger" @click="deleteItem(item)">删除</el-button>
          </div>
          <div style="padding: 10px;">
            <p class="result-prompt" :title="item.prompt">{{ item.prompt }}</p>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <el-tag :type="statusTagType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
              <span style="font-size: 11px; color: #909399;">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end; display: flex;"
      @change="loadData"
    />

    <el-dialog v-model="previewDialogVisible" width="70%" :title="'预览：' + (previewItem?.prompt || '')">
      <img v-if="previewItem" :src="'/api/result/file/' + previewItem.id" style="width: 100%; border-radius: 4px;" />
    </el-dialog>
  </el-card>
</template>

<script setup>
// ✅ 修复：移除 this.$axios
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { axios } from '../main.js'

const router = useRouter()
const filter = ref({ fileType: '', status: '', keyword: '' })
const page = ref(1)
const size = ref(12)
const total = ref(0)
const list = ref([])
const previewDialogVisible = ref(false)
const previewItem = ref(null)

const statusTagType = (s) => ({ PENDING: 'warning', PUBLISHED: 'success', DISCARDED: 'info' }[s] || 'info')
const statusLabel = (s) => ({ PENDING: '待发布', PUBLISHED: '已发布', DISCARDED: '已废弃' }[s] || s)
const formatTime = (t) => t ? t.substring(0, 16).replace('T', ' ') : ''
const onImgError = (e) => { e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="150"><rect fill="%23f5f5f5" width="200" height="150"/><text x="50%" y="50%" text-anchor="middle" fill="%23aaa">无图片</text></svg>' }

const loadData = async () => {
  try {
    const res = await axios.get('/result/list', {
      params: { page: page.value, size: size.value, ...filter.value }
    })
    list.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } catch (e) {
    console.error('加载成果失败', e)
  }
}

const resetFilter = () => {
  filter.value = { fileType: '', status: '', keyword: '' }
  page.value = 1
  loadData()
}

const preview = (item) => { previewItem.value = item; previewDialogVisible.value = true }
const publish = (item) => router.push('/publish?resultId=' + item.id)
const download = (item) => window.open('/api/result/file/' + item.id, '_blank')

const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该成果吗？删除后无法恢复', '提示', { type: 'warning' })
    await axios.delete('/result/' + item.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* 用户取消 */ }
}

onMounted(loadData)
</script>

<style scoped>
.result-card { overflow: hidden; }
.result-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}
.result-prompt {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0 0 6px;
}
.result-actions {
  position: absolute;
  inset: 0 0 68px 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.25s;
}
.result-card:hover .result-actions { opacity: 1; }
</style>
