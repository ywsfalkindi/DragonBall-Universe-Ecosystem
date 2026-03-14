import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      // PocketBase (files / images)
      // NOTE: keep this aligned with `PB_URL` (default: http://192.168.3.23:8090)
      {
        protocol: "http",
        hostname: "192.168.3.23",
        port: "8090",
        pathname: "/api/files/**",
      },

      // Common external sources used in this repo
      {
        protocol: "https",
        hostname: "**.top4top.io",
        pathname: "/**",
      },
      {
        protocol: "https",
        hostname: "commondatastorage.googleapis.com",
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;
