<template>
  <div class="max-w-3xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-2">エラー</h1>
    <p class="text-gray-600 mb-4">予期せぬエラーが発生しました。</p>
    <div class="flex gap-2">
      <button class="px-3 py-1.5 rounded border" @click="$router.go(0)">再読み込み</button>
      <NuxtLink to="/" class="px-3 py-1.5 rounded border">トップへ戻る</NuxtLink>
    </div>
  </div>
</template>
