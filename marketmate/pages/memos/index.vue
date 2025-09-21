<template>
  <div class="max-w-3xl mx-auto p-6 space-y-4">
    <h1 class="text-2xl font-bold">メモ履歴</h1>

    <div v-if="!authed" class="text-gray-600">
      閲覧にはログインが必要です。<NuxtLink to="/auth/login" class="underline">ログイン</NuxtLink>
    </div>

    <div v-else>
      <div v-if="items.length === 0" class="text-gray-600">まだメモがありません</div>

      <div v-for="m in items" :key="m.id" class="border rounded p-3 mb-3">
        <div class="flex items-start gap-2">
          <div class="flex-1 min-w-0">
            <!-- タイトル -->
            <div class="font-semibold truncate">{{ m.title || '(無題)' }}</div>

            <!-- 店名バッジ（強調） -->
            <div class="mt-1">
              <span
                class="inline-flex items-center gap-1 rounded-full border bg-gray-50 px-2 py-0.5 text-xs font-medium text-gray-800"
              >
                <span aria-hidden="true">🏬</span>
                <span>{{ placeNames[m.placeId] || '読み込み中…' }}</span>
              </span>
            </div>

            <!-- 日付 -->
            <div class="mt-1 text-xs text-gray-500">
              {{ new Date(m.updatedAt).toLocaleString('ja-JP') }}
            </div>
          </div>

          <button class="ml-auto px-2 py-1 rounded border shrink-0" @click="del(m.id)">削除</button>
        </div>

        <!-- メモ本体：そのまま全文表示 -->
        <div class="mt-3">
          <div v-if="memoDetails[m.id]?.lines?.length" class="text-sm space-y-1">
            <div v-for="(l, i) in memoDetails[m.id]!.lines" :key="i">{{ l }}</div>
          </div>
          <div v-else-if="memoDetails[m.id] === undefined" class="text-xs text-gray-400">
            読み込み中…
          </div>
          <div v-else class="text-xs text-red-600">メモの読み込みに失敗しました</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, onMounted, watch } from 'vue'
import { useAuth } from '../../composables/useAuth'
import { useMemos } from '../../composables/useMemos'
import { useApi } from '../../composables/useApi'

const { user } = useAuth()
const authed = computed(() => !!user.value)

const { list, refresh, remove } = useMemos()
const items = computed(() => list())
const api = useApi()

type MemoDetail = { lines: string[] }
const memoDetails: Record<string, MemoDetail | null | undefined> = reactive({})

const placeNames: Record<string, string> = reactive({})

async function fetchDetailIfNeeded(id: string) {
  if (memoDetails[id] !== undefined) return
  memoDetails[id] = undefined
  const m = items.value.find((x) => x.id === id)
  memoDetails[id] = m ? { lines: m.lines || [] } : null
}

async function fetchPlaceNameIfNeeded(placeId: string) {
  if (placeNames[placeId]) return
  placeNames[placeId] = ''
  const url = `/gapi/places/${encodeURIComponent(placeId)}/details`
  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      const d: any = await $fetch(url)
      const name = d?.displayName?.text ?? d?.displayName ?? d?.name ?? ''
      if (!name) throw new Error('no name')
      placeNames[placeId] = name
      return
    } catch (e) {
      if (attempt === 0) {
        await new Promise((r) => setTimeout(r, 250))
        continue
      }
      console.error('place name fetch failed:', placeId, e)
      placeNames[placeId] = '(取得失敗)'
    }
  }
}

onMounted(async () => {
  await refresh()
  for (const m of items.value) {
    fetchDetailIfNeeded(m.id)
    fetchPlaceNameIfNeeded(m.placeId)
  }
})

watch(items, (arr) => {
  for (const m of arr) {
    fetchDetailIfNeeded(m.id)
    fetchPlaceNameIfNeeded(m.placeId)
  }
})

async function del(id: string) {
  await remove(id)
  delete memoDetails[id]
}
</script>
