<template>
  <footer
    class="border-t bg-white/80 backdrop-blur supports-[backdrop-filter]:bg-white/60 sticky top-[100vh]"
  >
    <div class="mx-auto max-w-5xl px-4 sm:px-6">
      <div class="flex items-center justify-between py-4">
        <p class="text-xs text-gray-500">© 2025 MarketMate. All rights reserved.</p>

        <div class="flex items-center gap-4">
          <NuxtLink to="/terms" class="text-xs text-gray-500 hover:text-gray-700"
            >利用規約</NuxtLink
          >
          <NuxtLink to="/privacy" class="text-xs text-gray-500 hover:text-gray-700"
            >プライバシー</NuxtLink
          >

          <!-- Top ボタン（アイコンのみ / スムーススクロール） -->
          <button
            @click="toTop"
            aria-label="ページ先頭へ"
            class="group inline-flex items-center justify-center rounded-full border px-2.5 py-1.5 text-xs text-gray-500 hover:bg-gray-50 transition"
          >
            <svg
              class="h-4 w-4 transition-transform group-hover:-translate-y-0.5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 19V5m0 0l-6 6m6-6l6 6" />
            </svg>
            <span class="sr-only">Top</span>
          </button>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
const toTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
</script>
