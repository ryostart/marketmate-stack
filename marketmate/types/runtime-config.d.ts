export {}

declare module 'nuxt/schema' {
  interface RuntimeConfig {
    google: { searchApiKey: string }
  }
  interface PublicRuntimeConfig {
    apiBase: string
    google: { embedApiKey: string; mapsJsApiKey: string }
  }
}
