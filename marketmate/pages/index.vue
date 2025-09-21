<template>
  <main class="max-w-6xl mx-auto px-6 py-12 space-y-12">
    <!-- Hero -->
    <section class="text-center space-y-6">
      <h1 class="text-3xl md:text-5xl font-extrabold tracking-tight">
        近くのスーパーを、<span class="underline decoration-4">一撃検索</span>。
      </h1>
      <p class="text-gray-600 text-lg md:text-xl">
        駅名・路線名でサクッと探して、営業中・営業時間やレビューまで同一画面で。
      </p>
      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <NuxtLink to="/supermarkets" class="px-6 py-3 rounded bg-black text-white font-medium">
          スーパーを探す
        </NuxtLink>
        <NuxtLink to="/auth/register" class="px-6 py-3 rounded border font-medium">
          新規登録してはじめる
        </NuxtLink>
      </div>
    </section>

    <!-- ざっくり機能 -->
    <section class="grid md:grid-cols-3 gap-6">
      <div class="border rounded-2xl p-6">
        <h3 class="font-semibold mb-2">大きな地図＋複数ピン</h3>
        <p class="text-gray-600 text-sm">一覧の上に Google マップ。ピン→詳細へ。</p>
      </div>
      <div class="border rounded-2xl p-6">
        <h3 class="font-semibold mb-2">営業中・営業時間</h3>
        <p class="text-gray-600 text-sm">お探しのスーパーの詳細を表示。</p>
      </div>
      <div class="border rounded-2xl p-6">
        <h3 class="font-semibold mb-2">お気に入り／レビュー／メモ</h3>
        <p class="text-gray-600 text-sm">
          ログインで使用可能。スーパー検索は未ログインでも使用できます。
        </p>
      </div>
    </section>
  </main>
</template>
<script setup lang="ts">
import { useHead } from 'nuxt/app'
useHead({ title: 'MarketMate｜近くのスーパー検索' })
</script>
