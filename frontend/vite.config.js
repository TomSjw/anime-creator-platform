import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        cookieDomainRewrite: {
          'localhost': 'localhost'
        },
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            const setCookie = proxyRes.headers['set-cookie']
            if (setCookie) {
              proxyRes.headers['set-cookie'] = setCookie.map(cookie =>
                  cookie.replace(/; SameSite=None/gi, '; SameSite=Lax')
                      .replace(/; Secure/gi, '')
              )
            }
          })
        }
      }
    }
  }
})
