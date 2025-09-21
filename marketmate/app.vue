<template>
  <div class="min-h-screen flex flex-col">
    <HeaderNav />
    <main class="flex-1 pb-12">
      <NuxtErrorBoundary>
        <NuxtPage />
        <template #error="{ error, clearError }">
          <div class="max-w-3xl mx-auto p-6">
            <h2 class="text-xl font-bold mb-2">画面の表示に失敗しました</h2>
            <p class="mb-4 text-gray-600">{{ (error as any).message || '不明なエラー' }}</p>
            <div class="flex gap-2">
              <button class="px-3 py-1.5 rounded border" @click="clearError()">再試行</button>
              <NuxtLink to="/" class="px-3 py-1.5 rounded border">トップへ戻る</NuxtLink>
            </div>
          </div>
        </template>
      </NuxtErrorBoundary>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import HeaderNav from './components/HeaderNav.vue'
import SiteFooter from './components/SiteFooter.vue'
</script>
