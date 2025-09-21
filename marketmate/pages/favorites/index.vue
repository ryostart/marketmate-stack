<template>
  <div class="max-w-6xl mx-auto p-6 space-y-4">
    <h1 class="text-2xl font-bold">お気に入り一覧</h1>

    <div v-if="ids.length === 0" class="text-gray-600">
      まだお気に入りがありません。<NuxtLink to="/supermarkets" class="underline"
        >スーパー検索</NuxtLink
      >へ
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 左：お気に入り一覧 -->
      <div>
        <div class="grid gap-3 md:grid-cols-2">
          <div v-for="id in ids" :key="id" class="border rounded p-3">
            <div class="flex items-start gap-2">
              <!-- 店名（スケルトン） -->
              <div class="font-medium truncate min-w-0 flex-1">
                <template v-if="details[id]">
                  {{ details[id]!.displayName?.text }}
                </template>
                <span
                  v-else
                  class="inline-block h-4 w-2/3 rounded bg-gray-200 animate-pulse"
                ></span>
              </div>
              <FavoriteToggle :placeId="id" class="ml-auto shrink-0" />
            </div>

            <div class="mt-2 flex gap-2">
              <button @click="openFavDetail(id)" class="px-3 py-1.5 border rounded">詳細</button>
              <!-- Google マップボタンは非表示 -->
            </div>
          </div>
        </div>
      </div>

      <!-- 右：詳細（マスター/ディテール） -->
      <div>
        <div v-if="selectedId" class="border rounded p-4">
          <div class="flex items-start justify-between gap-3 mb-2">
            <h2 class="font-bold text-lg">
              {{ detail?.displayName?.text || '読み込み中…' }}
            </h2>
            <button @click="closeFavDetail" class="text-gray-500 hover:text-black">×</button>
          </div>

          <div v-if="loadingDetail" class="text-gray-600">読み込み中…</div>

          <div v-else-if="detail">
            <!-- 営業中/営業時間（Enterprise有効時のみ） -->
            <p
              v-if="detail.currentOpeningHours?.openNow"
              class="inline-block bg-emerald-100 text-emerald-700 text-xs px-2 py-1 rounded"
            >
              営業中
            </p>

            <div class="mt-2" v-if="detail.regularOpeningHours?.weekdayDescriptions?.length">
              <h3 class="font-semibold mb-1 text-sm">営業時間</h3>
              <ul class="text-sm leading-6">
                <li v-for="(w, idx) in detail.regularOpeningHours!.weekdayDescriptions!" :key="idx">
                  {{ w }}
                </li>
              </ul>
            </div>

            <!-- 地図（Embed API / place_id） -->
            <div class="mt-3 aspect-video">
              <iframe
                v-if="embedSrc"
                :src="embedSrc"
                class="w-full h-full rounded"
                loading="lazy"
                allowfullscreen
                referrerpolicy="no-referrer-when-downgrade"
              />
            </div>
          </div>

          <div v-else class="text-red-600">詳細の読み込みに失敗しました</div>
        </div>

        <!-- 案内：一覧があるときだけ -->
        <div v-else-if="ids.length > 0" class="text-gray-600">
          左の一覧から店舗を選択してください
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FavoriteToggle from '../../components/FavoriteToggle.vue'
import { useFavorites } from '../../composables/useFavorites'
import type { PlaceDetailsForHours } from '../../types/place'
import { computed, reactive, watchEffect, ref } from 'vue'
import { useRuntimeConfig } from 'nuxt/app'

const fav = useFavorites()
const ids = computed(() => fav.list.value)
const details = reactive<Record<string, PlaceDetailsForHours | undefined>>({})

watchEffect(async () => {
  for (const id of ids.value) {
    if (details[id] !== undefined) continue
    try {
      details[id] = await $fetch<PlaceDetailsForHours>(
        `/gapi/places/${encodeURIComponent(id)}/details`
      )
    } catch {
      details[id] = undefined
    }
  }
})

// 右ペイン：詳細表示用
const selectedId = ref<string | null>(null)
const detail = ref<PlaceDetailsForHours | null>(null)
const loadingDetail = ref(false)

// Maps Embed API（place_id で埋め込み）
const config = useRuntimeConfig()
const embedSrc = computed(() =>
  selectedId.value
    ? `https://www.google.com/maps/embed/v1/place?key=${encodeURIComponent(
        (config.public as any)?.google?.embedApiKey || ''
      )}&q=place_id:${encodeURIComponent(selectedId.value)}&language=ja&region=JP`
    : ''
)

async function openFavDetail(id: string) {
  selectedId.value = id
  loadingDetail.value = true
  detail.value = null
  try {
    const res = await $fetch<PlaceDetailsForHours>(`/gapi/places/${encodeURIComponent(id)}/details`)
    detail.value = res
  } finally {
    loadingDetail.value = false
  }
}

function closeFavDetail() {
  selectedId.value = null
  detail.value = null
}
</script>
