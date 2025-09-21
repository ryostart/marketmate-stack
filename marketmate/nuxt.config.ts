import { defineNuxtConfig } from 'nuxt/config'
import tsconfigPaths from 'vite-tsconfig-paths'
import { fileURLToPath, URL } from 'node:url'

export default defineNuxtConfig({
  pages: true,
  modules: ['@nuxtjs/tailwindcss'],
  // Tailwind のエントリCSS
  css: ['@/assets/css/tailwind.css'],

  // APIキーは server/public で分離（public はブラウザに露出）
  runtimeConfig: {
    google: {
      // server 専用（Places REST 用）
      searchApiKey: process.env.NUXT_GOOGLE_SEARCH_API_KEY,
    },
    public: {
      // フロント公開（埋め込み/JSマップ）
      google: {
        embedApiKey: process.env.NUXT_PUBLIC_GOOGLE_EMBED_API_KEY,
        mapsJsApiKey: process.env.NUXT_PUBLIC_GOOGLE_MAPS_JS_API_KEY,
      },
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
    },
  },

  // Vite 側で alias を正規表現バインド（CSS も .nuxt 経由で確実に解決）
  vite: {
    plugins: [tsconfigPaths()],
    resolve: {
      alias: [
        { find: /^@\//, replacement: fileURLToPath(new URL('./', import.meta.url)) },
        { find: /^~\//, replacement: fileURLToPath(new URL('./', import.meta.url)) },
      ],
    },
  },
})
