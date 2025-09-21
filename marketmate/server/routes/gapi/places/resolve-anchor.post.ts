import { defineEventHandler, readBody, createError } from 'h3'
import { useRuntimeConfig } from '#imports'

// キーワード（駅など）→ 代表座標を 1 件だけ解決（補助用途）
export default defineEventHandler(async (event) => {
  const { query } = await readBody<{ query: string }>(event)
  const { google } = useRuntimeConfig(event)
  if (!query?.trim()) return { anchor: null }
  try {
    const res = await $fetch<any>('https://places.googleapis.com/v1/places:searchText', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Goog-Api-Key': google.searchApiKey,
        'X-Goog-FieldMask': 'places.id,places.displayName,places.location',
      },
      body: { textQuery: query, languageCode: 'ja', regionCode: 'JP' },
    })
    const p = res?.places?.[0]
    return p?.location
      ? {
          anchor: {
            placeId: p.id,
            name: p.displayName?.text ?? query,
            lat: p.location.latitude,
            lng: p.location.longitude,
          },
        }
      : { anchor: null }
  } catch (e: any) {
    throw createError({
      statusCode: e?.status || 502,
      statusMessage: '検索位置の解決に失敗しました。',
    })
  }
})
