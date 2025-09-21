<template>
  <!-- 未ログイン: フォーム非表示でログイン導線 -->
  <div v-if="!authed" class="text-sm text-gray-600">
    レビューを投稿するには
    <NuxtLink class="underline" :to="`/auth/login?next=${encodeURIComponent(route.fullPath)}`"
      >ログイン</NuxtLink
    >
    が必要です。
  </div>

  <!-- ログイン済み: 投稿フォーム -->
  <div v-else>
    <div class="flex items-center gap-3">
      <RatingInput v-model="rating" />
      <span class="text-xs text-gray-500">評価</span>
    </div>

    <textarea v-model="comment" class="mt-2 w-full" placeholder="コメントを入力"></textarea>

    <div class="mt-2 flex items-center gap-2">
      <button class="px-3 py-1 rounded bg-black text-white" @click="submit">投稿</button>
      <span v-if="err" class="text-red-600 text-sm">{{ err }}</span>
      <span v-if="ok" class="text-green-600 text-sm">投稿しました</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import RatingInput from './RatingInput.vue'
import { useReviews } from '../composables/useReviews'
import { useAuth } from '../composables/useAuth'
import { useRoute } from '#imports'

type Review = { id: string; userId: string; rating: number; comment: string; createdAt: string }

const props = defineProps<{ placeId: string }>()
const emit = defineEmits<{ (e: 'posted', review: Review): void }>()

const { user } = useAuth()
const authed = computed(() => !!user.value)
const route = useRoute()

const { add } = useReviews()

const rating = ref(3)
const comment = ref('')
const err = ref<string | null>(null)
const ok = ref(false)

async function submit() {
  err.value = null
  ok.value = false
  if (!comment.value.trim()) {
    err.value = 'コメントを入力してください'
    return
  }
  try {
    const created = await add(props.placeId, Number(rating.value), comment.value.trim())
    emit('posted', created)
    comment.value = ''
    ok.value = true
  } catch (e) {
    console.error(e)
    err.value = '投稿に失敗しました'
  }
}
</script>
