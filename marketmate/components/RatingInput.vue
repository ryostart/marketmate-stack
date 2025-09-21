<template>
  <div
    class="inline-flex items-center"
    role="radiogroup"
    aria-label="星で評価"
    tabindex="0"
    @keydown.prevent="onKey"
    @mouseleave="hover = null"
  >
    <button
      v-for="i in 5"
      :key="i"
      type="button"
      role="radio"
      :aria-checked="modelValue === i"
      :aria-label="`${i} 星`"
      class="p-1 rounded focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-500"
      @mouseenter="hover = i"
      @focus="hover = i"
      @blur="hover = null"
      @click="setValue(i)"
    >
      <!-- 選択値（hover中はhover優先）まで塗りつぶし -->
      <svg
        v-if="i <= displayValue"
        viewBox="0 0 24 24"
        class="h-5 w-5 text-yellow-400"
        aria-hidden="true"
      >
        <path
          fill="currentColor"
          d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
        />
      </svg>
      <svg v-else viewBox="0 0 24 24" class="h-5 w-5 text-gray-300" aria-hidden="true">
        <path
          fill="currentColor"
          d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
        />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = withDefaults(defineProps<{ modelValue: number }>(), { modelValue: 3 })
const emit = defineEmits<{ (e: 'update:modelValue', v: number): void }>()

const hover = ref<number | null>(null)
const displayValue = computed(() => hover.value ?? props.modelValue)

function setValue(i: number) {
  // 1〜5 の整数に正規化
  const v = Math.min(5, Math.max(1, Math.round(i)))
  emit('update:modelValue', v)
}

function onKey(e: KeyboardEvent) {
  const step = 1
  if (e.key === 'ArrowRight' || e.key === 'ArrowUp') setValue(props.modelValue + step)
  if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') setValue(props.modelValue - step)
  if (e.key === 'Home') setValue(1)
  if (e.key === 'End') setValue(5)
}
</script>
