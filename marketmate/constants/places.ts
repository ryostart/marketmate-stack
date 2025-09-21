export const INCLUDED_TYPE_SUPERMARKET = 'supermarket' as const
export const DEFAULT_RADIUS_M = 1000 as const

// 一覧で必要なフィールドに限定（FieldMask）
export const FIELD_MASK_LIST = [
  'places.id',
  'places.name',
  'places.displayName',
  'places.primaryType',
  'places.types',
  'places.location',
  'places.shortFormattedAddress',
  'places.googleMapsUri',
].join(',')

// 詳細（営業時間）はさらに絞る
export const FIELD_MASK_DETAILS_HOURS = [
  'id',
  'displayName',
  'currentOpeningHours',
  'regularOpeningHours',
].join(',')

// 詳細ペインの地図（Embed API：無料）
export const buildEmbedSrc = (
  key: string,
  placeId: string,
  opt?: { lang?: string; region?: string; zoom?: number }
) =>
  `https://www.google.com/maps/embed/v1/place?key=${key}&q=place_id:${encodeURIComponent(placeId)}&language=${opt?.lang ?? 'ja'}&region=${opt?.region ?? 'JP'}&zoom=${opt?.zoom ?? 16}`
