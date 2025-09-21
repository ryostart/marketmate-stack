<template>
  <div class="max-w-2xl mx-auto p-6 space-y-4">
    <h1 class="text-2xl font-bold">買い物メモ作成</h1>

    <div v-if="!authed" class="text-gray-600">
      作成にはログインが必要です。<NuxtLink to="/auth/login" class="underline">ログイン</NuxtLink>
    </div>

    <div v-else class="space-y-3">
      <FormField label="お気に入りスーパー（必須）" :error="favError">
        <select v-model="placeId" class="border rounded px-3 py-2 w-full">
          <option value="">選択してください</option>
          <option v-for="id in favIds" :key="id" :value="id">
            {{ placeNames[id] || '読み込み中…' }}
          </option>
        </select>
      </FormField>

      <FormField label="タイトル">
        <input v-model="title" class="border rounded px-3 py-2 w-full" />
      </FormField>

      <FormField label="メモ（1行=1品）">
        <textarea v-model="text" class="border rounded px-3 py-2 w-full min-h-[160px]"></textarea>
      </FormField>

      <button class="px-4 py-2 rounded bg-black text-white" @click="save">保存</button>
      <p v-if="msg" class="text-sm text-green-700">{{ msg }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, watch, onMounted } from 'vue'
import { useAuth } from '../../composables/useAuth'
import { useFavorites } from '../../composables/useFavorites'
import { useMemos } from '../../composables/useMemos'
import type { PlaceDetailsForHours } from '../../types/place'
import FormField from '../../components/FormField.vue'

const { user } = useAuth()
const authed = computed(() => !!user.value)

const fav = useFavorites()
const favIds = computed(() => fav.list.value)

// 店名表示用キャッシュ
const placeDetails = reactive<Record<string, PlaceDetailsForHours | null | undefined>>({})
const placeNames = computed<Record<string, string>>(() => {
  const m: Record<string, string> = {}
  for (const id of favIds.value) m[id] = placeDetails[id]?.displayName?.text ?? ''
  return m
})
async function fetchPlaceIfNeeded(id: string) {
  if (placeDetails[id] !== undefined) return
  placeDetails[id] = null
  try {
    placeDetails[id] = await $fetch<PlaceDetailsForHours>(
      `/gapi/places/${encodeURIComponent(id)}/details`
    )
  } catch {
    placeDetails[id] = null
  }
}
onMounted(() => {
  favIds.value.forEach(fetchPlaceIfNeeded)
})
watch(favIds, (ids) => {
  ids.forEach(fetchPlaceIfNeeded)
})

const placeId = ref<string>('')
const title = ref('')
const text = ref('')

const msg = ref('')
const favError = ref<string | null>(null)

const memos = useMemos()

async function save() {
  msg.value = ''
  favError.value = null

  const pid = placeId.value.trim()
  if (!pid) {
    favError.value = 'お気に入りスーパーを選択してください'
    return
  }

  const lines = text.value
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)

  await memos.add(pid, title.value.trim(), lines)
  msg.value = '保存しました'
  title.value = ''
  text.value = ''
  setTimeout(() => {
    msg.value = ''
  }, 3000)
}
</script>
