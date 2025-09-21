import { ref, type Ref } from 'vue'
import { useAuth } from './useAuth'
import { useApi } from './useApi'

export type Review = {
  id: string
  userId: string
  placeId: string
  rating: number
  comment: string
  createdAt: string
}

// ---- グローバルに 1 個だけ持つ（相対パス違いでも同じインスタンスを共有） ----
type ReviewsStore = { byPlace: Ref<Record<string, Review[]>> }
const STORE_KEY = '__mm_reviews_store__'
const _store: ReviewsStore =
  (globalThis as any)[STORE_KEY] ??
  ((globalThis as any)[STORE_KEY] = { byPlace: ref<Record<string, Review[]>>({}) })

const byPlace = _store.byPlace

export function useReviews() {
  const { user } = useAuth()
  const api = useApi()

  async function fetch(placeId: string) {
    byPlace.value[placeId] = await api.$get<Review[]>('/reviews', {
      query: { placeId },
    })
  }

  // 投稿 → 楽観更新 → サーバ確定で置換（即時反映用に Promise<Review> を返す）
  async function add(placeId: string, rating: number, comment: string): Promise<Review> {
    if (!user.value) throw new Error('ログインが必要です')

    const prev = byPlace.value[placeId] ?? []
    const optimistic: Review = {
      id: 'tmp-' + Math.random().toString(36).slice(2),
      userId: user.value.id,
      placeId,
      rating,
      comment,
      createdAt: new Date().toISOString(),
    }
    byPlace.value[placeId] = [optimistic, ...prev]

    try {
      const created = await api.$post<Review>('/reviews', {
        body: { placeId, rating, comment },
      })
      byPlace.value[placeId] = [created, ...prev]
      return created
    } catch (e) {
      byPlace.value[placeId] = prev // 失敗時はロールバック
      throw e
    }
  }

  function list(placeId: string) {
    return byPlace.value[placeId] ?? []
  }

  async function summary(placeId: string) {
    return await api.$get<{ count: number; average: number }>('/reviews/summary', {
      query: { placeId },
    })
  }

  return { fetch, add, list, summary }
}
