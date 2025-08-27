import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
      <div className="max-w-md mx-auto text-center p-6">
        <div className="bg-slate-800/50 backdrop-blur-sm border border-slate-700 rounded-xl p-8 shadow-xl">
          <div className="text-6xl mb-4">🎬</div>
          <h2 className="text-3xl font-bold text-white mb-4">404</h2>
          <h3 className="text-xl font-semibold text-slate-300 mb-4">Page Not Found</h3>
          <p className="text-slate-400 mb-6">
            The page you're looking for doesn't exist. It might have been moved, deleted, or you entered the wrong URL.
          </p>
          <div className="space-y-3">
            <Link
              href="/"
              className="block w-full bg-primary-600 hover:bg-primary-700 text-white font-medium py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Go Home
            </Link>
            <Link
              href="/admin"
              className="block w-full bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Admin Dashboard
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}