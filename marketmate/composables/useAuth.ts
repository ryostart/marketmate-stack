type SessionUser = { id: string; name: string; email?: string }

export function useAuth() {
  // Nuxt useState に永続（タブ間でも共有）
  const user = useState<SessionUser | null>('auth:user', () => null)
  const { $get, $post } = useApi()

  async function me() {
    // Cookie が生きていればユーザを取得
    const u = await $get<SessionUser>('/auth/me')
    user.value = u ?? null
    return user.value
  }

  async function login(email: string, password: string) {
    await $post('/auth/login', { body: { email, password } })
    await me()
  }

  async function register(displayName: string, email: string, password: string) {
    await $post('/auth/signup', { body: { displayName, email, password } })
    await me()
  }

  async function logout() {
    user.value = null
    try {
      await $post('/auth/logout')
    } finally {
      user.value = null
      if (process.client) {
        await navigateTo('/')
      }
    }
  }

  return { user, me, login, register, logout }
}
