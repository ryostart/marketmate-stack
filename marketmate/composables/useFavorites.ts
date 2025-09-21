import { ref, computed, onMounted } from 'vue'
import { useAuth } from './useAuth'
import { useApi } from './useApi'

type FavoriteDto = {
  placeId: string
  userId?: string
  createdAt?: number
}

export function useFavorites() {
  const { user } = useAuth()
  const api = useApi()
  const ids = ref<string[]>([])
  const loading = ref(false)

  // ---- サーバーから最新取得（ログイン時のみ）----
  async function refresh() {
    if (!user.value) {
      // 未ログインはフォールバック
      ids.value = []
      return
    }
    loading.value = true
    try {
      // サーバーは userId を Cookie から取得する想定（クエリは渡さない）
      const rows = await api.$get<FavoriteDto[] | string[]>('/favorites')
      // 返却が FavoriteDto[] でも string[](placeIdだけ) でも動くように吸収
      ids.value = (rows as any[]).map((r) => (typeof r === 'string' ? r : r.placeId))
    } finally {
      loading.value = false
    }
  }

  // ---- トグル（ログイン必須）----
  async function toggle(placeId: string) {
    if (!user.value) {
      // 既存の動き：未ログインは例外を投げてボタン側で握りつぶす想定
      throw new Error('ログインが必要です')
    }

    const exists = ids.value.includes(placeId)

    // 先にUI更新（楽観的更新）
    ids.value = exists ? ids.value.filter((id) => id !== placeId) : [...ids.value, placeId]

    try {
      if (exists) {
        // DELETE /api/favorites/{placeId}
        await api.$delete(`/favorites/${encodeURIComponent(placeId)}`)
      } else {
        // POST /api/favorites  body: { placeId } だけ
        await api.$post<FavoriteDto>('/favorites', { body: { placeId } })
      }
    } catch (e) {
      // 失敗時は元へ戻す（既存の振る舞いを維持）
      ids.value = exists ? [...ids.value, placeId] : ids.value.filter((id) => id !== placeId)
      console.error(e)
      throw e
    }
  }

  const has = (placeId: string) => ids.value.includes(placeId)

  onMounted(refresh)

  return {
    list: computed(() => ids.value),
    has,
    toggle,
    refresh,
    loading,
  }
}
