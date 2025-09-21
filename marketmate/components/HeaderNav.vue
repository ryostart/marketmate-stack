<template>
  <header class="border-b bg-white/70 backdrop-blur sticky top-0 z-40">
    <div class="max-w-6xl mx-auto px-4 py-3 flex items-center gap-4">
      <!-- ランディングへ戻る -->
      <NuxtLink to="/" class="font-bold text-lg">MarketMate</NuxtLink>

      <!-- グローバルナビ -->
      <nav class="flex gap-3 text-sm">
        <NuxtLink to="/supermarkets" class="hover:underline">スーパー検索</NuxtLink>
        <NuxtLink to="/favorites" class="hover:underline">お気に入り一覧</NuxtLink>
        <NuxtLink to="/memos/create" class="hover:underline">メモ作成</NuxtLink>
        <NuxtLink to="/memos" class="hover:underline">メモ履歴</NuxtLink>
      </nav>

      <!-- 認証 -->
      <div class="ml-auto flex items-center gap-2 text-sm">
        <template v-if="user">
          <span class="text-gray-600 hidden sm:inline"
            >ようこそ、{{ user.name || user.email }}</span
          >
          <button class="px-2 py-1 rounded border" @click="logout">ログアウト</button>
        </template>
        <template v-else>
          <NuxtLink to="/auth/login" class="px-2 py-1 rounded border">ログイン</NuxtLink>
          <NuxtLink to="/auth/register" class="px-2 py-1 rounded border">新規登録</NuxtLink>
        </template>
      </div>
    </div>
  </header>
</template>
<script setup lang="ts">
import { useAuth } from '../composables/useAuth'
const { user, logout } = useAuth()
</script>
