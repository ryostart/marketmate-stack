<template>
  <!-- 親から高さを受け取る（w-full/h-full） -->
  <div ref="mapEl" class="w-full h-full"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, toRefs } from 'vue'
import { useRuntimeConfig } from 'nuxt/app'
import type { PlaceListItem } from '../types/place'

const props = defineProps<{ places: PlaceListItem[]; selectedId?: string | null }>()
const emit = defineEmits<{ (e: 'select', placeId: string): void }>()
const { places, selectedId } = toRefs(props)
const mapEl = ref<HTMLDivElement | null>(null)

let map: google.maps.Map | null = null
let markers = new Map<string, google.maps.Marker>()
let infoWindow: google.maps.InfoWindow | null = null

// Maps JS の動的ロード（既にロード済みならスキップ）
function loadGoogleMaps(apiKey: string): Promise<void> {
  if (typeof window === 'undefined') return Promise.resolve()
  if ((window as any).google?.maps) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const id = 'gmap-js'
    if (document.getElementById(id)) return resolve()
    const s = document.createElement('script')
    s.id = id
    s.async = true
    s.defer = true
    s.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&v=weekly`
    s.onload = () => resolve()
    s.onerror = (e) => reject(e)
    document.head.appendChild(s)
  })
}

// マーカー再生成（一覧変化時）
function rebuildMarkers() {
  if (!map) return
  // 既存をクリア
  markers.forEach((m) => m.setMap(null))
  markers.clear()
  const bounds = new google.maps.LatLngBounds()
  let hasBounds = false

  for (const p of places.value || []) {
    const lat = p.location?.latitude,
      lng = p.location?.longitude
    if (typeof lat !== 'number' || typeof lng !== 'number') continue
    const pos = { lat, lng }
    const m = new google.maps.Marker({
      position: pos,
      map,
      title: p.displayName?.text ?? '(名称不明)',
    })
    m.addListener('click', () => emit('select', p.id))
    markers.set(p.id, m)
    bounds.extend(pos)
    hasBounds = true
  }

  if (hasBounds) {
    // 2件以上→全体フィット、1件→寄せ
    if ((places.value?.length || 0) >= 2) map.fitBounds(bounds, 24)
    else {
      const only = places.value!.find((p) => p.location?.latitude && p.location?.longitude)!
      map.setCenter({ lat: only.location!.latitude!, lng: only.location!.longitude! })
      map.setZoom(16)
    }
  }
}

// 選択中をフォーカス＆InfoWindow
function focusSelected() {
  if (!map || !selectedId?.value) return
  const m = markers.get(selectedId.value)
  if (!m) return
  map.panTo(m.getPosition()!)
  map.setZoom(Math.max(map.getZoom() || 0, 16))
  const target = (props.places || []).find((x) => x.id === selectedId.value)
  const name = target?.displayName?.text ?? '(名称不明)'
  const addr = target?.shortFormattedAddress ?? ''
  infoWindow ??= new google.maps.InfoWindow()
  infoWindow.setContent(`<strong>${name}</strong><br>${addr}`)
  infoWindow.open({ map, anchor: m })
}

onMounted(async () => {
  const key = (useRuntimeConfig().public.google as any)?.mapsJsApiKey as string | undefined
  if (!key) return // キー未設定なら何もしない（空表示）
  await loadGoogleMaps(key)
  if (!mapEl.value) return
  map = new google.maps.Map(mapEl.value, {
    center: { lat: 35.681236, lng: 139.767125 },
    zoom: 12,
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: true,
  })
  rebuildMarkers()
  focusSelected()
})
onBeforeUnmount(() => {
  markers.forEach((m) => m.setMap(null))
  markers.clear()
  infoWindow?.close()
  map = null
})
watch(places, () => rebuildMarkers(), { deep: true })
watch(selectedId, () => focusSelected())
</script>
