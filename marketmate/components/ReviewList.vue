<template>
  <div class="space-y-3">
    <div v-if="items.length === 0" class="text-sm text-gray-600">まだレビューはありません</div>

    <div v-for="r in items" :key="r.id" class="border rounded p-3">
      <div class="flex items-center gap-2 leading-none">
        <RatingDisplay :value="r.rating" />
        <span class="text-xs text-gray-500">{{ formatDate(r.createdAt) }}</span>
      </div>
      <p class="mt-1 whitespace-pre-wrap text-sm">{{ r.comment }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import RatingDisplay from './RatingDisplay.vue'

type Review = {
  id: string
  userId: string
  rating: number
  comment: string
  createdAt: string | number
}
const props = defineProps<{ items: Review[] }>()

function formatDate(ts: string | number) {
  try {
    const d = new Date(typeof ts === 'number' ? ts : String(ts))
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}/${m}/${day}`
  } catch {
    return ''
  }
}
</script>
