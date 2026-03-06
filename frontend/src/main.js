import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import axios from 'axios'

import 'element-plus/dist/index.css'

// ✅ 修复：配置 axios 并导出，供各 View 直接 import 使用
// Vue3 script setup 中无法使用 this，不能通过 app.config.globalProperties 的方式访问
axios.defaults.baseURL = '/api'
axios.defaults.withCredentials = true

axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export { axios }  // ✅ 导出供各页面直接 import

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
