export type PlaceListItem = {
  id: string
  displayName?: { text?: string }
  shortFormattedAddress?: string
  location?: { latitude: number; longitude: number }
  googleMapsUri?: string
}
export type OpeningHours = { openNow?: boolean; weekdayDescriptions?: string[] }
export type PlaceDetailsForHours = {
  id: string
  displayName?: { text?: string }
  currentOpeningHours?: OpeningHours
  regularOpeningHours?: OpeningHours
}
