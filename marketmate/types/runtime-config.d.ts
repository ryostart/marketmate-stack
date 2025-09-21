export {}
declare module 'nuxt/schema' {
  interface RuntimeConfig {
    google: { searchApiKey: string }
  }
  interface PublicRuntimeConfig {
    google: { embedApiKey: string; mapsJsApiKey: string }
    firebase: { apiKey: string; authDomain: string; projectId: string; appId: string }
  }
}
