// server/utils/places.ts
// Google Places (New) の薄いラッパー。
// ・pageTokenだけの続き取得に対応（余計な引数は送らない）
// ・includedTypesを任意で渡せる
// ・getDetailsHours を追加（営業時間などの詳細取得）

type BaseRes<T = any> = { places?: T[]; nextPageToken?: string | null }

// ================== Text Search ==================
export type TextParams = {
  pageToken?: string
  textQuery?: string
  languageCode?: string
  regionCode?: string
  locationBias?: {
    circle: { center: { latitude: number; longitude: number }; radius: number }
  }
  fieldMask?: string
}

export async function searchText(apiKey: string, p: TextParams): Promise<BaseRes> {
  const url = 'https://places.googleapis.com/v1/places:searchText'
  const body: any = p.pageToken
    ? { pageToken: p.pageToken } // 続きはトークンのみ
    : {
        textQuery: p.textQuery ?? '',
        languageCode: p.languageCode ?? 'ja',
        regionCode: p.regionCode ?? 'JP',
        ...(p.locationBias ? { locationBias: p.locationBias } : {}),
      }

  const fieldMask =
    p.fieldMask ||
    [
      'places.id',
      'places.displayName',
      'places.shortFormattedAddress',
      'places.location',
      'places.types',
    ].join(',')

  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'X-Goog-Api-Key': apiKey,
      'X-Goog-FieldMask': fieldMask,
    },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const txt = await res.text().catch(() => '')
    throw Object.assign(new Error('searchText failed'), { status: res.status, data: txt })
  }
  const data = await res.json()
  return { places: data.places ?? [], nextPageToken: data.nextPageToken ?? null }
}

// ================== Nearby Search ==================
export type NearbyParams = {
  pageToken?: string
  lat?: number
  lng?: number
  radiusMeters?: number
  includedTypes?: string[]
  languageCode?: string
  regionCode?: string
  fieldMask?: string
}

export async function searchNearby(apiKey: string, p: NearbyParams): Promise<BaseRes> {
  const url = 'https://places.googleapis.com/v1/places:searchNearby'

  const body: any = p.pageToken
    ? { pageToken: p.pageToken } // 続きはトークンのみ
    : {
        languageCode: p.languageCode ?? 'ja',
        regionCode: p.regionCode ?? 'JP',
        maxResultCount: 20,
        ...(p.includedTypes ? { includedTypes: p.includedTypes } : {}),
        locationRestriction: {
          circle: {
            center: { latitude: p.lat!, longitude: p.lng! },
            radius: p.radiusMeters ?? 2500,
          },
        },
      }

  const fieldMask =
    p.fieldMask ||
    [
      'places.id',
      'places.displayName',
      'places.shortFormattedAddress',
      'places.location',
      'places.types',
    ].join(',')

  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'X-Goog-Api-Key': apiKey,
      'X-Goog-FieldMask': fieldMask,
    },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const txt = await res.text().catch(() => '')
    throw Object.assign(new Error('searchNearby failed'), { status: res.status, data: txt })
  }
  const data = await res.json()
  return { places: data.places ?? [], nextPageToken: data.nextPageToken ?? null }
}

// ================== Details（営業時間など） ==================
export type DetailsOpts = {
  languageCode?: string
  regionCode?: string
  fieldMask?: string
}

/**
 * 営業中フラグ / 通常営業時間など、詳細を取得
 * PlaceDetailsForHours に相当するフィールドのみ取得して課金最小化
 */
export async function getDetailsHours(
  apiKey: string,
  placeId: string,
  opts: DetailsOpts = {}
): Promise<any> {
  const url = `https://places.googleapis.com/v1/places/${encodeURIComponent(placeId)}`
  // 単一GETの FieldMask は "places." プレフィックス不要
  const fieldMask =
    opts.fieldMask ||
    [
      'id',
      'displayName',
      'formattedAddress',
      'location',
      'types',
      'currentOpeningHours.openNow',
      'regularOpeningHours.weekdayDescriptions',
    ].join(',')

  const qp = new URLSearchParams()
  qp.set('languageCode', opts.languageCode ?? 'ja')
  qp.set('regionCode', opts.regionCode ?? 'JP')

  const res = await fetch(`${url}?${qp.toString()}`, {
    method: 'GET',
    headers: {
      'X-Goog-Api-Key': apiKey,
      'X-Goog-FieldMask': fieldMask,
    },
  })

  if (!res.ok) {
    const txt = await res.text().catch(() => '')
    throw Object.assign(new Error('getDetailsHours failed'), { status: res.status, data: txt })
  }

  return await res.json()
}
