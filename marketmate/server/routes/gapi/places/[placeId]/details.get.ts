import { defineEventHandler, getRouterParam, createError } from 'h3'
import { useRuntimeConfig } from '#imports'
import { getDetailsHours } from '~~/server/utils/places'
import type { PlaceDetailsForHours } from '~~/types/place'

// 詳細：営業中/営業時間など最小限
export default defineEventHandler(async (event): Promise<PlaceDetailsForHours> => {
  const id = getRouterParam(event, 'placeId')!
  const { google } = useRuntimeConfig(event)
  try {
    return await getDetailsHours(google.searchApiKey, id)
  } catch (e: any) {
    throw createError({
      statusCode: e?.status || 502,
      statusMessage: '店舗詳細の取得に失敗しました。',
    })
  }
})
