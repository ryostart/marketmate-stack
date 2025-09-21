import { ref, computed } from 'vue'
import { passwordPolicy as basePasswordPolicy } from '../utils/validators'

type KeyOf<T> = Extract<keyof T, string>
export type Validator<T> = (value: any, form: T) => string | null
export type ValidationRules<T> = Partial<Record<KeyOf<T>, Validator<T>[]>>

export function useValidation<T>(rules: ValidationRules<T>) {
  // 内部表現は Record<string, ...> / Set<string> に統一して型トラブルを回避
  const errors = ref<Record<string, string | null>>({})
  const touched = ref<Set<string>>(new Set())

  const ruleKeys = computed(() => Object.keys(rules) as KeyOf<T>[])

  function setError(key: KeyOf<T>, msg: string | null) {
    errors.value[key as string] = msg
  }
  function getError(key: KeyOf<T>): string | null {
    return errors.value[key as string] ?? null
  }
  function touch(key: KeyOf<T>) {
    touched.value.add(key as string)
  }
  function clear(key?: KeyOf<T>) {
    if (key === undefined) {
      errors.value = {}
      touched.value = new Set()
      return
    }
    delete errors.value[key as string]
    touched.value.delete(key as string)
  }

  function validateField<K extends KeyOf<T>>(key: K, value: T[K], form: T): string | null {
    const fns = (rules[key] ?? []) as Validator<T>[]
    for (const fn of fns) {
      const msg = fn(value, form)
      if (msg) {
        setError(key, msg)
        return msg
      }
    }
    setError(key, null)
    return null
  }

  function validateAll(form: T): boolean {
    for (const k of ruleKeys.value) validateField(k, (form as any)[k], form)
    return ruleKeys.value.every((k) => getError(k) == null)
  }

  const isValid = computed(() => ruleKeys.value.every((k) => errors.value[k as string] == null))

  return { errors, touched, isValid, setError, getError, touch, clear, validateField, validateAll }
}

/* ルール群（使い方：rules: { email: [required(), email()] } など） */
export const required =
  (msg = '必須項目です'): Validator<any> =>
  (v: any) => {
    if (typeof v === 'string') return v.trim() ? null : msg
    if (Array.isArray(v)) return v.length ? null : msg
    return v !== null && v !== undefined ? null : msg
  }

export const email =
  (msg = 'メールアドレスの形式が正しくありません'): Validator<any> =>
  (v: any) => {
    if (typeof v !== 'string') return msg
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim()) ? null : msg
  }

/** 共通validatorsの passwordPolicy を“ルール”にラップ（重複exportを避ける） */
export const passwordRule =
  (msg = '8文字以上・英字と数字を各1つ以上含めてください'): Validator<any> =>
  (v: any) => {
    const res = basePasswordPolicy(typeof v === 'string' ? v : '')
    return res === true ? null : typeof res === 'string' ? res : msg
  }

export const equalTo =
  <T>(otherKey: KeyOf<T>, msg = '入力が一致しません'): Validator<T> =>
  (_value: any, form: T) =>
    (form as any)[otherKey] === _value ? null : msg
