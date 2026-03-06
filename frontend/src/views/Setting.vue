<template>
  <el-card>
    <h2>系统设置</h2>
    <el-tabs v-model="activeTab" style="margin-top: 20px;">

      <!-- AI工具配置 -->
      <el-tab-pane label="AI工具配置" name="ai">
        <el-table :data="aiTools" border style="width: 100%;">
          <el-table-column prop="toolName" label="工具名称" />
          <el-table-column prop="toolType" label="类型" width="80" />
          <el-table-column prop="apiUrl" label="API地址" />
          <el-table-column prop="isEnabled" label="启用" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isEnabled ? 'success' : 'info'" size="small">{{ row.isEnabled ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-alert type="info" :closable="false" style="margin-top: 16px;"
          description="如需修改 AI 工具地址，请直接编辑后端 application.yml 中的配置，或在数据库 ai_tool_config 表中操作。" />
      </el-tab-pane>

      <!-- 小红书配置 -->
      <el-tab-pane label="小红书配置" name="xhs">
        <el-form :model="xhsForm" label-width="120px" style="max-width: 580px;">
          <el-form-item label="AppID">
            <el-input v-model="xhsForm.appId" placeholder="小红书开放平台 AppID" />
          </el-form-item>
          <el-form-item label="AppSecret">
            <el-input v-model="xhsForm.appSecret" type="password" placeholder="小红书开放平台 AppSecret" show-password />
          </el-form-item>
          <el-form-item label="回调地址">
            <el-input v-model="xhsForm.redirectUri" placeholder="授权回调地址" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveXhsConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 系统设置 -->
      <el-tab-pane label="系统设置" name="system">
        <el-form :model="systemForm" label-width="150px" style="max-width: 580px;">
          <el-form-item label="成果存储路径">
            <el-input v-model="systemForm.storagePath" placeholder="/data/anime/storage" />
          </el-form-item>
          <el-form-item label="任务超时时间(秒)">
            <el-input-number v-model="systemForm.timeout" :min="30" :max="3600" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSystemConfig">保存设置</el-button>
          </el-form-item>
        </el-form>
        <el-alert type="warning" :closable="false" style="margin-top: 16px; max-width: 580px;"
          description="修改存储路径后，需重启后端服务生效，且不会迁移已有文件。" />
      </el-tab-pane>

      <!-- 个人中心 -->
      <el-tab-pane label="个人中心" name="user">
        <el-form :model="userForm" label-width="120px" style="max-width: 500px;">
          <el-form-item label="用户名">
            <el-input v-model="userForm.username" disabled />
          </el-form-item>
          <el-form-item label="旧密码">
            <el-input v-model="userForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="userForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="userForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
// ✅ 修复：移除 this.$axios，所有操作直接调用 axios
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { axios } from '../main.js'

const activeTab = ref('ai')

const aiTools = ref([
  { toolName: 'Stable Diffusion', toolType: 'IMAGE', apiUrl: 'http://127.0.0.1:7860/sdapi/v1', isEnabled: true }
])

const xhsForm = ref({ appId: '', appSecret: '', redirectUri: '' })
const systemForm = ref({ storagePath: '', timeout: 300 })
const userForm = ref({ username: 'admin', oldPassword: '', newPassword: '', confirmPassword: '' })

const saveXhsConfig = () => {
  // TODO: 调用后端保存配置接口
  ElMessage.success('配置已保存（功能待后端接口支持）')
}

const saveSystemConfig = () => {
  // TODO: 调用后端保存配置接口
  ElMessage.success('设置已保存（功能待后端接口支持）')
}

const changePassword = async () => {
  if (!userForm.value.oldPassword || !userForm.value.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (userForm.value.newPassword !== userForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  // TODO: 调用后端修改密码接口
  ElMessage.success('密码修改功能待后端接口支持')
}
</script>
