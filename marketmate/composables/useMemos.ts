import { ref, type Ref } from 'vue'
import { useAuth } from './useAuth'
import { useApi } from './useApi'

export type MemoItem = {
  id: string
  userId: string
  placeId: string
  title: string
  lines: string[]
  updatedAt: string
}

type Store = { list: Ref<MemoItem[]> }
const KEY = '__mm_memos_store__'
const _store: Store =
  (globalThis as any)[KEY] ?? ((globalThis as any)[KEY] = { list: ref<MemoItem[]>([]) })
const _list = _store.list

export function useMemos() {
  const { user } = useAuth()
  const api = useApi()

  const list = () => _list.value

  async function refresh() {
    if (!user.value) {
      _list.value = []
      return
    }
    // userId は送らず、Cookie ベースでサーバが判定
    _list.value = await api.$get<MemoItem[]>('/memos')
  }

  async function add(placeId: string, title: string, lines: string[]) {
    if (!user.value) throw new Error('ログインが必要です')
    await api.$post('/memos', {
      body: { placeId, title, lines },
    })
    await refresh()
  }

  async function remove(id: string) {
    if (!user.value) throw new Error('ログインが必要です')
    await api.$delete(`/memos/${encodeURIComponent(id)}`)
    await refresh()
  }

  onMounted(refresh)

  return { list, refresh, add, remove }
}
