FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
# Copy the built Vite assets to NGINX
COPY --from=builder /app/dist /usr/share/nginx/html
# Expose port 80
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]