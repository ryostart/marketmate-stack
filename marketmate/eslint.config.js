// marketmate/eslint.config.js
import js from '@eslint/js'
import * as tseslint from 'typescript-eslint'
import vue from 'eslint-plugin-vue'
import nuxt from 'eslint-plugin-nuxt'
import vueParser from 'vue-eslint-parser'
import prettier from 'eslint-config-prettier'
import globals from 'globals'

export default [
  { ignores: ['.nuxt/**', '.output/**', 'dist/**', 'node_modules/**', 'coverage/**', '**/*.min.*'] },

  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...vue.configs['flat/recommended'],
  ...(nuxt.configs?.['flat/recommended'] ?? []),

  // .vue (<script lang="ts">)
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: { parser: tseslint.parser, ecmaVersion: 'latest', sourceType: 'module' },
      globals: { ...globals.browser, google: 'readonly', $fetch: 'readonly', useValidation: 'readonly' }
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/attributes-order': 'off',
      'vue/attribute-hyphenation': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      'no-empty': ['off', { allowEmptyCatch: true }]
    }
  },

  // .ts/.js 共通の緩和
  {
    files: ['**/*.{ts,js}'],
    languageOptions: { globals: globals.browser },
    rules: {
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      'no-empty': ['off', { allowEmptyCatch: true }]
    }
  },

  {
    files: ['**/*.d.ts'],
    rules: {
      '@typescript-eslint/triple-slash-reference': 'off'
    }
  },

  prettier
]
