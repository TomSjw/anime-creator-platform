<template>
  <el-card>
    <h2>成果管理</h2>
    <!-- 筛选栏 -->
    <div style="margin: 20px 0; display: flex; gap: 10px; align-items: center;">
      <el-select v-model="filter.fileType" placeholder="文件类型" clearable style="width: 120px;">
        <el-option label="图片" value="IMAGE"></el-option>
        <el-option label="视频" value="VIDEO"></el-option>
      </el-select>
      <el-select v-model="filter.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="待发布" value="PENDING"></el-option>
        <el-option label="已发布" value="PUBLISHED"></el-option>
        <el-option label="已废弃" value="DISCARDED"></el-option>
      </el-select>
      <el-input v-model="filter.keyword" placeholder="搜索提示词" clearable style="width: 200px;"></el-input>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <!-- 成果列表 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in list" :key="item.id">
        <el-card class="result-card">
          <img :src="'/api/result/file/' + item.id" class="result-img" />
          <div class="result-info">
            <p class="result-prompt">{{ item.prompt }}</p>
            <p class="result-time">{{ item.createTime }}</p>
            <el-tag :type="item.status === 'PUBLISHED' ? 'success' : item.status === 'DISCARDED' ? 'info' : 'warning'">
              {{ item.status }}
            </el-tag>
          </div>
          <div class="result-actions">
            <el-button size="small" @click="preview(item)">预览</el-button>
            <el-button size="small" type="success" @click="publish(item)" v-if="item.status === 'PENDING'">发布</el-button>
            <el-button size="small" @click="download(item)">下载</el-button>
            <el-button size="small" type="danger" @click="deleteItem(item)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      style="margin-top: 20px; text-align: right;"
      @change="loadData"
    />

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" width="80%">
      <img v-if="previewItem" :src="'/api/result/file/' + previewItem.id" style="width: 100%;" />
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const filter = ref({
  fileType: '',
  status: '',
  keyword: ''
})
const page = ref(1)
const size = ref(12)
const total = ref(0)
const list = ref([])
const previewDialogVisible = ref(false)
const previewItem = ref(null)

const loadData = async () => {
  try {
    const params = {
      page: page.value,
      size: size.value,
      ...filter.value
    }
    const res = await this.$axios.get('/result/list', { params })
    list.value = res.data.data.records
    total.value = res.data.data.total
  } catch(e) {
    console.error(e)
  }
}

const resetFilter = () => {
  filter.value = {
    fileType: '',
    status: '',
    keyword: ''
  }
  page.value = 1
  loadData()
}

const preview = (item) => {
  previewItem.value = item
  previewDialogVisible.value = true
}

const publish = (item) => {
  router.push('/publish?resultId=' + item.id)
}

const download = (item) => {
  window.open('/api/result/file/' + item.id, '_blank')
}

const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该成果吗？删除后无法恢复', '提示')
    await this.$axios.delete('/result/' + item.id)
    ElMessage.success('删除成功')
    loadData()
  } catch(e) {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.result-card {
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}
.result-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.result-info {
  padding: 10px;
}
.result-prompt {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.result-time {
  font-size: 12px;
  color: #909399;
}
.result-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 80px;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}
.result-card:hover .result-actions {
  opacity: 1;
}
</style>
