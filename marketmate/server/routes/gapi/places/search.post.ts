// server/api/places/search.post.ts
// 方針: つねに Text Search。キーワードに「スーパー」を自動付与。
//       座標があれば locationBias を付与（順位のヒント）。pageToken は単独で送る。
import { defineEventHandler, readBody, createError } from 'h3'
import { useRuntimeConfig } from '#imports'
import { searchText } from '~~/server/utils/places'
import type { PlaceListItem } from '~~/types/place'
import { DEFAULT_RADIUS_M } from '~~/constants/places'

type ReqBody = {
  textQuery?: string
  lat?: number
  lng?: number
  radiusMeters?: number
  pageToken?: string
}

const isNum = (v: unknown): v is number => typeof v === 'number' && Number.isFinite(v)
const clamp = (n: number, lo: number, hi: number) => Math.max(lo, Math.min(hi, n))
const MIN_RADIUS = 200
const MAX_RADIUS = 5000

// 「スーパー」語が無ければ付与（英日ゆる判定）
const hasGroceryWord = (q: string) => /スーパー|supermarket|grocery|食料|食品|マーケット/i.test(q)
const ensureSuperTerm = (q: string) => {
  const s = (q || '').trim()
  if (!s) return 'スーパー'
  return hasGroceryWord(s) ? s : `${s} スーパー`
}

export default defineEventHandler(
  async (event): Promise<{ places?: PlaceListItem[]; nextPageToken?: string | null }> => {
    const { textQuery = '', lat, lng, radiusMeters, pageToken } = await readBody<ReqBody>(event)
    const { google } = useRuntimeConfig(event)

    if (!google?.searchApiKey) {
      throw createError({
        statusCode: 500,
        statusMessage: 'Places APIキーが未設定です（.env の NUXT_GOOGLE_SEARCH_API_KEY を確認）',
      })
    }

    const hasCoord = isNum(lat) && isNum(lng)
    const radius = clamp(
      isNum(radiusMeters) ? radiusMeters : DEFAULT_RADIUS_M,
      MIN_RADIUS,
      MAX_RADIUS
    )
    const q = ensureSuperTerm(textQuery)

    try {
      // 1) 続き取得：pageToken だけ送る（他条件は付けない）
      if (pageToken) {
        return await searchText(google.searchApiKey, { pageToken })
      }

      // 2) Text Search（includedType は送らない）
      const body: any = {
        textQuery: q,
        languageCode: 'ja',
        regionCode: 'JP',
      }
      if (hasCoord) {
        body.locationBias = {
          circle: { center: { latitude: lat!, longitude: lng! }, radius },
        }
      }

      return await searchText(google.searchApiKey, body)
    } catch (e: any) {
      const msg = e?.data?.error?.message || e?.statusMessage || 'スーパーの検索に失敗しました。'
      throw createError({ statusCode: e?.status || 502, statusMessage: msg })
    }
  }
)
