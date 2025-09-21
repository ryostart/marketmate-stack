export function useApi() {
  const { public: pub } = useRuntimeConfig()
  const baseURL = (pub as any).apiBase as string
  const router = useRouter()

  const common = {
    baseURL,
    credentials: 'include' as const,
    headers: { 'Content-Type': 'application/json' },
  }

  const onError = (err: any) => {
    const status = err?.response?.status ?? err?.status
    if (status === 401) {
      // 認証状態クリア
      try {
        const { user } = useAuth()
        user.value = null
      } catch {}

      // SSR 中は遷移しない / すでに認証ページならループしない
      if (process.client && !location.pathname.startsWith('/auth/')) {
        const next = encodeURIComponent(location.pathname + location.search)
        router.push(`/auth/login?next=${next}`)
      }
    }
    throw err
  }

  const $get = <T>(url: string, opts: any = {}) =>
    $fetch<T>(url, { ...common, method: 'GET', ...opts }).catch(onError)
  const $post = <T>(url: string, opts: any = {}) =>
    $fetch<T>(url, { ...common, method: 'POST', ...opts }).catch(onError)
  const $put = <T>(url: string, opts: any = {}) =>
    $fetch<T>(url, { ...common, method: 'PUT', ...opts }).catch(onError)
  const $del = <T>(url: string, opts: any = {}) =>
    $fetch<T>(url, { ...common, method: 'DELETE', ...opts }).catch(onError)

  const $delete = $del // エイリアス

  return { $get, $post, $put, $del, $delete }
}
