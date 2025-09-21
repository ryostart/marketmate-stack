<template>
  <button
    type="button"
    class="inline-flex items-center"
    :aria-pressed="isFav"
    :title="isFav ? 'お気に入り解除' : 'お気に入りに追加'"
    @click.stop.prevent="onClick"
  >
    <!-- アイコン -->
    <svg
      class="w-5 h-5"
      :class="isFav ? 'text-yellow-400 fill-current' : 'text-gray-400'"
      viewBox="0 0 20 20"
      aria-hidden="true"
    >
      <path
        d="M10 15l-5.878 3.09L5.64 11.545.76 7.41l6.18-.9L10 1l3.06 5.51 6.18.9-4.88 4.135 1.518 6.545z"
      />
    </svg>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useFavorites } from '../composables/useFavorites'

const props = defineProps<{ placeId: string }>()
const fav = useFavorites()
const isFav = computed(() => fav.has(props.placeId))
const onClick = async () => {
  try {
    await fav.toggle(props.placeId)
  } catch (e) {
    /* noop */
  }
}
</script>
