<template>
  <el-card>
    <h2>系统设置</h2>
    <el-tabs v-model="activeTab" style="margin-top: 20px;">
      <!-- AI工具配置 -->
      <el-tab-pane label="AI工具配置" name="ai">
        <el-table :data="aiTools" border style="width: 100%;">
          <el-table-column prop="toolName" label="工具名称"></el-table-column>
          <el-table-column prop="toolType" label="工具类型"></el-table-column>
          <el-table-column prop="apiUrl" label="API地址"></el-table-column>
          <el-table-column prop="priority" label="优先级"></el-table-column>
          <el-table-column prop="isEnabled" label="是否启用">
            <template #default="scope">
              <el-tag :type="scope.row.isEnabled ? 'success' : 'info'">{{ scope.row.isEnabled ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <el-button size="small">编辑</el-button>
            <el-button size="small" type="danger">删除</el-button>
          </el-table-column>
        </el-table>
        <el-button type="primary" style="margin-top: 20px;">添加AI工具</el-button>
      </el-tab-pane>

      <!-- 小红书配置 -->
      <el-tab-pane label="小红书配置" name="xhs">
        <el-form :model="xhsForm" label-width="120px" style="max-width: 600px;">
          <el-form-item label="AppID">
            <el-input v-model="xhsForm.appId" placeholder="小红书开放平台AppID"></el-input>
          </el-form-item>
          <el-form-item label="AppSecret">
            <el-input v-model="xhsForm.appSecret" placeholder="小红书开放平台AppSecret"></el-input>
          </el-form-item>
          <el-form-item label="回调地址">
            <el-input v-model="xhsForm.redirectUri" placeholder="授权回调地址"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">保存配置</el-button>
            <el-button type="success">绑定小红书账号</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 系统设置 -->
      <el-tab-pane label="系统设置" name="system">
        <el-form :model="systemForm" label-width="150px" style="max-width: 600px;">
          <el-form-item label="成果存储路径">
            <el-input v-model="systemForm.storagePath"></el-input>
          </el-form-item>
          <el-form-item label="任务超时时间(秒)">
            <el-input v-model="systemForm.timeout" type="number"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">保存设置</el-button>
            <el-button>备份数据</el-button>
            <el-button>恢复数据</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 个人中心 -->
      <el-tab-pane label="个人中心" name="user">
        <el-form :model="userForm" label-width="120px" style="max-width: 500px;">
          <el-form-item label="用户名">
            <el-input v-model="userForm.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="userForm.nickname"></el-input>
          </el-form-item>
          <el-form-item label="旧密码">
            <el-input v-model="userForm.oldPassword" type="password"></el-input>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="userForm.newPassword" type="password"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="userForm.confirmPassword" type="password"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeTab = ref('ai')
const aiTools = ref([
  // 示例数据，后续可动态加载
  {
    toolName: 'Stable Diffusion',
    toolType: 'IMAGE',
    apiUrl: 'http://127.0.0.1:7860/sdapi/v1',
    priority: 1,
    isEnabled: true
  }
])
const xhsForm = ref({
  appId: '',
  appSecret: '',
  redirectUri: ''
})
const systemForm = ref({
  storagePath: '/home/user/Desktop/anime-creator-platform/storage',
  timeout: 300
})
const userForm = ref({
  username: 'admin',
  nickname: '管理员',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  // 加载配置数据
})
</script>
