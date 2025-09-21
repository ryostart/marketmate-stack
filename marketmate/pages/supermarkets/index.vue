<template>
  <div class="max-w-6xl mx-auto p-6 space-y-4">
    <h1 class="text-2xl font-bold">スーパー検索</h1>

    <!-- 検索フォーム -->
    <div class="space-y-2">
      <label class="block text-sm font-medium">検索キーワード（駅名/路線名）</label>
      <div class="flex gap-2">
        <input
          v-model="q"
          @keyup.enter="search"
          class="border rounded px-3 py-2 w-full max-w-md"
          placeholder="例: 渋谷駅 / 山手線"
        />
        <button class="px-4 py-2 rounded bg-black text-white" @click="search">検索</button>
      </div>
    </div>

    <!-- 大きな Google マップ（一覧を覆う） -->
    <ClientOnly>
      <div class="w-full h-[360px] md:h-[420px] rounded border overflow-hidden">
        <ResultsMapGmap :places="results" :selected-id="selectedId" @select="select" />
      </div>
    </ClientOnly>

    <!-- 2カラム: 左=一覧 / 右=詳細 -->
    <div class="grid gap-6 md:grid-cols-2">
      <!-- 左: 一覧 -->
      <div>
        <div v-if="loading" class="text-gray-600">検索中…</div>

        <div v-else class="space-y-3">
          <div v-for="p in pagedResults" :key="p.id" class="border rounded p-3">
            <div class="flex items-start gap-2">
              <div class="font-medium truncate">{{ p.displayName?.text ?? '(名称不明)' }}</div>
              <FavoriteToggle :placeId="p.id" class="ml-auto" />
            </div>
            <div class="text-sm text-gray-600 overflow-hidden text-ellipsis">
              {{ p.shortFormattedAddress }}
            </div>
            <button class="mt-2 px-3 py-1.5 border rounded" @click="select(p.id)">
              → 右で詳細を開く
            </button>
          </div>
        </div>

        <!-- ページネーション -->
        <div v-if="results.length > 0" class="mt-3 flex items-center gap-3">
          <button
            class="px-3 py-1.5 rounded border"
            :disabled="currentPage <= 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            前へ
          </button>

          <span class="text-sm text-gray-600">ページ {{ currentPage }} / {{ totalPages }}</span>

          <button
            class="px-3 py-1.5 rounded border"
            :disabled="currentPage >= totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
          >
            次へ
          </button>
        </div>
      </div>

      <!-- 右: 詳細 -->
      <div>
        <div v-if="selectedId" class="border rounded p-4">
          <div class="flex items-start gap-2">
            <h2 class="font-semibold text-lg flex-1">{{ selectedTitle }}</h2>
            <button class="px-2 py-1 rounded border" @click="clear">×</button>
          </div>

          <div v-if="detail" class="mt-2 space-y-2">
            <div>
              営業中:
              <span
                :class="detail.currentOpeningHours?.openNow ? 'text-green-700' : 'text-gray-700'"
              >
                {{ detail.currentOpeningHours?.openNow ? '営業中' : '営業時間外' }}
              </span>
            </div>

            <div v-if="detail.regularOpeningHours?.weekdayDescriptions?.length">
              <div class="text-sm text-gray-600">営業時間（通常）</div>
              <ul class="list-disc pl-5 text-sm">
                <li v-for="(d, i) in detail.regularOpeningHours!.weekdayDescriptions!" :key="i">
                  {{ d }}
                </li>
              </ul>
            </div>

            <!-- 埋め込み地図（無料の Embed API） -->
            <div v-if="embedKey" class="mt-3">
              <div class="relative pt-[56.25%]">
                <!-- 16:9 -->
                <iframe
                  class="absolute inset-0 w-full h-full rounded border"
                  :src="buildEmbedSrc(embedKey, selectedId!)"
                  loading="lazy"
                  allowfullscreen
                  referrerpolicy="no-referrer-when-downgrade"
                />
              </div>
            </div>

            <!-- レビュー -->
            <div class="mt-4">
              <h3 class="font-medium mb-2">レビュー</h3>
              <ReviewList :items="reviews" />
              <div class="mt-3">
                <ReviewForm v-if="authed && selectedId" :placeId="selectedId!" @posted="onPosted" />
                <div v-else class="text-sm text-gray-600">レビュー投稿にはログインが必要です。</div>
              </div>
            </div>
          </div>

          <div v-else>詳細を読み込み中…</div>
        </div>

        <!-- 案内（結果があるときだけ） -->
        <div v-else-if="results.length > 0" class="text-gray-600">
          左の一覧から店舗を選択してください
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect, watch } from 'vue'
import { useRoute, useRouter, useRuntimeConfig } from 'nuxt/app'

import FavoriteToggle from '../../components/FavoriteToggle.vue'
import ReviewList from '../../components/ReviewList.vue'
import ReviewForm from '../../components/ReviewForm.vue'
import ResultsMapGmap from '../../components/ResultsMapGmap.vue'

import { buildEmbedSrc } from '../../constants/places'
import type { PlaceListItem, PlaceDetailsForHours } from '../../types/place'
import { useReviews } from '../../composables/useReviews'

import { useAuth } from '../../composables/useAuth'

/* ----- state ----- */
const q = ref('')
const results = ref<PlaceListItem[]>([])
const selectedId = ref<string | null>(null)
const detail = ref<PlaceDetailsForHours | null>(null)
const loading = ref(false)

/* ----- router/config ----- */
const route = useRoute()
const router = useRouter()
const cfg = useRuntimeConfig()
const embedKey = (cfg.public.google as any)?.embedApiKey as string | undefined

/* ----- reviews store（詳細切替で必ずfetch） ----- */
const { list: listReviews, fetch: fetchReviews } = useReviews()
const reviews = computed(() => (selectedId.value ? listReviews(selectedId.value) : []))
watch(selectedId, (id) => {
  if (id) fetchReviews(id)
})

const { user } = useAuth()
const authed = computed(() => !!user.value)

/* ----- paginate (client-only) ----- */
const PAGE_SIZE = 10
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(results.value.length / PAGE_SIZE)))
const pagedResults = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return results.value.slice(start, start + PAGE_SIZE)
})
watch(results, () => {
  currentPage.value = 1
})

/* ----- タイトル（詳細取得前のプレースホルダ名対応） ----- */
const selectedPreviewName = ref('')
const selectedTitle = computed(
  () => detail.value?.displayName?.text ?? selectedPreviewName.value ?? '店舗詳細'
)

/* ----- helpers ----- */
function findNameById(id: string): string {
  const p = results.value.find((x) => x.id === id)
  return p?.displayName?.text || ''
}

/* ----- URL ?id と同期：開いたら詳細をロード ----- */
watchEffect(async () => {
  const id = (route.query.id as string) || null
  if (id && id !== selectedId.value) {
    selectedId.value = id
    selectedPreviewName.value = findNameById(id) // 見出しのチラツキ防止
    loadDetail(id)
  }
})

/* ----- actions ----- */
async function search() {
  // 新しい検索の前に詳細を閉じる
  clear()
  loading.value = true
  try {
    // 駅名→座標の解決（成功したらTextSearchの位置バイアスに使われる）
    let lat: number | undefined, lng: number | undefined
    try {
      const anc = await $fetch<{ anchor: { lat: number; lng: number } | null }>(
        '/gapi/places/resolve-anchor',
        { method: 'POST', body: { query: q.value } }
      )
      lat = anc.anchor?.lat
      lng = anc.anchor?.lng
    } catch {
      /* 失敗しても続行 */
    }

    // TextSearch（“スーパー”自動付与はサーバ側ロジックで実施）
    const res = await $fetch<{ places?: PlaceListItem[] }>('/gapi/places/search', {
      method: 'POST',
      body: { textQuery: q.value, lat, lng },
    })
    results.value = res.places ?? []

    // 念のため0件時の保険
    if ((results.value?.length ?? 0) === 0) {
      const res2 = await $fetch<{ places?: PlaceListItem[] }>('/gapi/places/search', {
        method: 'POST',
        body: { textQuery: `${q.value} スーパー`, lat, lng },
      })
      if ((res2.places?.length ?? 0) > 0) results.value = res2.places!
    }
  } catch (e) {
    console.error('検索エラー:', e)
    results.value = []
  } finally {
    loading.value = false
  }
}

async function loadDetail(id: string) {
  detail.value = null
  try {
    detail.value = await $fetch<PlaceDetailsForHours>(
      `/gapi/places/${encodeURIComponent(id)}/details`
    )
  } catch {
    detail.value = null
  }
}

function select(id: string) {
  // クリック直後から店名を見出しに表示
  selectedPreviewName.value = findNameById(id)
  router.replace({ query: { id } })
  loadDetail(id)
}

function clear() {
  router.replace({ query: {} })
  selectedId.value = null
  detail.value = null
  selectedPreviewName.value = ''
}

async function onPosted() {
  if (selectedId.value) await fetchReviews(selectedId.value)
}
</script>
