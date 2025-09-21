// eslint.config.js
import js from '@eslint/js'
import * as tseslint from 'typescript-eslint'
import vue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import nuxt from 'eslint-plugin-nuxt'
import eslintConfigPrettier from 'eslint-config-prettier'

export default [
  // 0) 無視パターン（ビルド物など）
  {
    ignores: ['.nuxt/**', '.output/**', 'dist/**', 'node_modules/**', 'coverage/**', '**/*.min.*'],
  },

  // 1) JS 基本
  js.configs.recommended,

  // 2) TS 推奨（型チェックなしで軽い）
  ...tseslint.configs.recommended,

  // 3) Vue / Nuxt 推奨
  ...vue.configs['flat/recommended'],
  nuxt.configs['flat/recommended'],

  // 4) .vue の <script lang="ts"> を TS でパース
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      // Nuxt では単語数1つのコンポーネント名を許容
      'vue/multi-word-component-names': 'off',
    },
  },

  // 5) よく使う軽量ルール
  {
    files: ['**/*.{js,ts,vue}'],
    rules: {
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'no-debugger': 'warn',
      // 使うなら↓（余裕があれば）
      // 'unused-imports/no-unused-imports': 'warn'
    },
  },

  // 6) Prettier と競合する“整形系ルール”を無効化
  eslintConfigPrettier,
]
