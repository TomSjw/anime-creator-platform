<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 30px;">私人动漫创作平台</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="login" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%;" :loading="loading" @click="login">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
// ✅ 修复：移除 this.$axios，改为直接 import axios
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { axios } from '../main.js'

const router = useRouter()
const loading = ref(false)
const form = ref({
  username: 'admin',
  password: '123456'
})

const login = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('username', form.value.username)
    params.append('password', form.value.password)

// 登录请求直接打后端，获取 Cookie
    const res = await axios.post('/auth/login', params)
    // 加这行，看看实际返回了什么
    console.log('登录响应：', res.status, JSON.stringify(res.data))

    // 只要没报错就算成功
    localStorage.setItem('isLoggedIn', 'true')
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    console.log('登录异常：', e.response?.status, JSON.stringify(e.response?.data))
    ElMessage.error('登录失败，用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  border-radius: 12px;
}
</style>
