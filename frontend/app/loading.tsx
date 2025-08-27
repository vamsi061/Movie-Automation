export default function Loading() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
      <div className="text-center">
        <div className="relative">
          <div className="w-16 h-16 border-4 border-slate-600 border-t-primary-500 rounded-full animate-spin mx-auto mb-4"></div>
          <div className="w-12 h-12 border-4 border-slate-700 border-t-purple-400 rounded-full animate-spin absolute top-2 left-1/2 transform -translate-x-1/2"></div>
        </div>
        <h2 className="text-xl font-semibold text-white mb-2">Loading Movie Links</h2>
        <p className="text-slate-400">Please wait while we prepare your experience...</p>
      </div>
    </div>
  )
}