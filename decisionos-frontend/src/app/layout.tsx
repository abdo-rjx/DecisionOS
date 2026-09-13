import type { Metadata } from "next";
import "../styles/globals.css";

export const metadata: Metadata = {
  title: "DecisionOS",
  description: "Decision-support simulator under uncertainty",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-gray-50 text-gray-900 antialiased">
        <div className="mx-auto max-w-6xl px-4 py-8">{children}</div>
      </body>
    </html>
  );
}
