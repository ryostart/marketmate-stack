// utils/validators.ts
// 共通バリデーション（最小8文字・英字/数字を各1つ以上）

/** パスワードポリシー: 最小8文字・英字/数字を各1つ以上 */
export function passwordPolicy(pw: string): true | string {
  if (!pw) return 'パスワードを入力してください'
  if (pw.length < 8) return 'パスワードは8文字以上で入力してください'
  if (!/[A-Za-z]/.test(pw)) return '英字を1文字以上含めてください'
  if (!/[0-9]/.test(pw)) return '数字を1文字以上含めてください'
  return true
}

/** メール形式チェック（簡易） */
export function validateEmail(email: string): true | string {
  if (!email) return 'メールアドレスを入力してください'
  const ok = /^\S+@\S+\.\S+$/.test(email)
  return ok ? true : 'メールアドレスの形式が正しくありません'
}
