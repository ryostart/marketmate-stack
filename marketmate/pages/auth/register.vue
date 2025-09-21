<template>
  <div class="max-w-sm mx-auto space-y-6 pt-10 md:pt-10">
    <h1 class="text-2xl font-bold tracking-tight mb-2">新規登録</h1>

    <FormField label="表示名" hint="必須">
      <input
        v-model="form.displayName"
        type="text"
        class="border rounded px-3 py-2 w-full"
        autocomplete="nickname"
        @blur="onBlur('displayName')"
      />
      <p v-if="err('displayName')" class="text-xs text-red-600">{{ err('displayName') }}</p>
    </FormField>

    <FormField label="メールアドレス">
      <input
        v-model="form.email"
        type="email"
        class="border rounded px-3 py-2 w-full"
        autocomplete="email"
        @blur="onBlur('email')"
      />
      <p v-if="err('email')" class="text-xs text-red-600">{{ err('email') }}</p>
    </FormField>

    <FormField label="パスワード">
      <input
        v-model="form.password"
        type="password"
        class="border rounded px-3 py-2 w-full"
        autocomplete="new-password"
        @blur="onBlur('password')"
      />
      <p v-if="err('password')" class="text-xs text-red-600">{{ err('password') }}</p>
    </FormField>

    <p v-if="msg" class="text-sm text-red-600">{{ msg }}</p>

    <button
      class="px-4 py-2 rounded bg-black text-white w-full"
      @click="submit"
      :disabled="submitting"
    >
      登録してはじめる
    </button>

    <p class="text-sm text-gray-600">
      既にアカウントをお持ちの方は
      <NuxtLink to="/auth/login" class="underline">ログイン</NuxtLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth'
import { required, email as emailRule, passwordRule } from '../../composables/useValidation'

const route = useRoute()
const router = useRouter()
const { register } = useAuth()

const form = reactive({
  displayName: '',
  email: '',
  password: '',
})

const v = useValidation<typeof form>({
  displayName: [required('表示名を入力してください')],
  email: [required('メールアドレスを入力してください'), emailRule()],
  password: [required('パスワードを入力してください'), passwordRule()],
})
const err = (k: keyof typeof form) => v.getError(k)
const onBlur = (k: keyof typeof form) => v.validateField(k, form[k], form)

const msg = ref('')
const submitting = ref(false)

async function submit() {
  msg.value = ''
  if (!v.validateAll(form)) return

  try {
    submitting.value = true
    await register(form.displayName.trim(), form.email.trim(), form.password)
    const next = (route.query.next as string) || '/supermarkets'
    router.push(next)
  } catch (e: any) {
    msg.value = e?.data?.message || e?.message || '登録に失敗しました'
  } finally {
    submitting.value = false
  }
}
</script>
