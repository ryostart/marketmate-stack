<template>
  <!-- 5つ並べ、埋まっている数だけ黄色。SVGは block でベースラインのズレ回避 -->
  <div class="inline-flex items-center leading-none">
    <svg
      v-for="i in 5"
      :key="i"
      viewBox="0 0 24 24"
      :class="['h-4 w-4 block', i <= filled ? 'text-yellow-400' : 'text-gray-300']"
      aria-hidden="true"
    >
      <path
        fill="currentColor"
        d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ value: number }>()
// 1刻み前提：四捨五入して 0〜5 にクリップ
const filled = computed(() => Math.max(0, Math.min(5, Math.round(props.value ?? 0))))
</script>
