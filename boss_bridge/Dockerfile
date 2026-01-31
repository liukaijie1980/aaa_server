# BOSS Bridge: SOAP to radiusApi 桥接服务
FROM node:20-alpine

WORKDIR /app

# 先复制依赖文件，利用层缓存
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev

# 复制应用代码
COPY . .

EXPOSE 8090

ENV NODE_ENV=production
CMD ["npm", "start"]
