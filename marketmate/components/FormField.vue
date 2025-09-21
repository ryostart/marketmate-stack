<template>
  <div class="space-y-1">
    <label v-if="label" :for="id" class="text-sm font-medium">{{ label }}</label>
    <slot />
    <p v-if="hint" class="text-xs text-gray-500">{{ hint }}</p>
    <p v-if="error" class="text-xs text-red-600">{{ error }}</p>
  </div>
</template>
<script setup lang="ts">
defineProps<{ id?: string; label?: string; hint?: string; error?: string | null }>()
</script>
