# ---- build stage ----
FROM node:20-alpine AS build
WORKDIR /app

# devDependencies を含めてインストール
# ※ ignore-scripts は外しておく方が安全
COPY package*.json ./
RUN npm ci

# アプリのソース投入
COPY . .

# Nuxt に渡す公開用環境変数
# docker-compose 側から --build-arg で注入
ARG NUXT_PUBLIC_API_BASE
ENV NUXT_PUBLIC_API_BASE=${NUXT_PUBLIC_API_BASE}

# 本番ビルド（nuxt が入っているので実行できる）
RUN npm run build

# ---- runtime stage ----
FROM node:20-alpine AS runner
WORKDIR /app

# ランタイムは production
ENV NODE_ENV=production
EXPOSE 3000

# ビルド成果物だけをコピー（軽量）
COPY --from=build /app/.output ./.output

# Nitro サーバー起動
CMD ["node", ".output/server/index.mjs"]
