'use client'

import { useState, useEffect } from 'react'
import { MagnifyingGlassIcon, LinkIcon, ClockIcon, CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline'
import { toast } from 'react-hot-toast'
import axios from 'axios'

interface MovieSite {
  id?: number
  siteName: string
  currentWorkingUrl?: string
  status: string
  lastChecked?: string
  lastUpdated?: string
  responseTime?: number
  searchAliases?: string[]
  isActive?: boolean
  notes?: string
}

interface SearchResult {
  success: boolean
  siteName?: string
  result?: MovieSite
  results?: MovieSite[]
  totalSites?: number
  error?: string
  timestamp: string
}

const POPULAR_SITES = [
  'movierulz',
  'moviezap', 
  'tamilrockers',
  'filmywap',
  'worldfree4u',
  '9xmovies',
  'khatrimaza',
  'bolly4u'
]

export default function Home() {
  const [searchTerm, setSearchTerm] = useState('')
  const [isSearching, setIsSearching] = useState(false)
  const [searchResults, setSearchResults] = useState<MovieSite[]>([])
  const [selectedSites, setSelectedSites] = useState<string[]>([])
  const [isBatchSearching, setIsBatchSearching] = useState(false)

  const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

  // Search single site
  const searchSingleSite = async (siteName: string) => {
    setIsSearching(true)
    try {
      const response = await axios.get<SearchResult>(`${API_URL}/api/movie-sites/search/${siteName}`)
      
      if (response.data.success && response.data.result) {
        setSearchResults([response.data.result])
        toast.success(`Found working link for ${siteName}!`)
      } else {
        toast.error(`No working link found for ${siteName}`)
        setSearchResults([])
      }
    } catch (error) {
      console.error('Search error:', error)
      toast.error(`Failed to search for ${siteName}`)
      setSearchResults([])
    } finally {
      setIsSearching(false)
    }
  }

  // Search multiple sites
  const searchMultipleSites = async (siteNames: string[]) => {
    setIsBatchSearching(true)
    try {
      const response = await axios.post<SearchResult>(`${API_URL}/api/movie-sites/search/batch`, {
        siteNames
      })
      
      if (response.data.success && response.data.results) {
        setSearchResults(response.data.results)
        const workingCount = response.data.results.filter(site => site.status === 'WORKING').length
        toast.success(`Found ${workingCount} working links out of ${siteNames.length} sites!`)
      } else {
        toast.error('Batch search failed')
        setSearchResults([])
      }
    } catch (error) {
      console.error('Batch search error:', error)
      toast.error('Failed to perform batch search')
      setSearchResults([])
    } finally {
      setIsBatchSearching(false)
    }
  }

  // Search all popular sites
  const searchAllSites = async () => {
    await searchMultipleSites(POPULAR_SITES)
  }

  // Handle single search
  const handleSingleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (!searchTerm.trim()) {
      toast.error('Please enter a site name')
      return
    }
    searchSingleSite(searchTerm.trim().toLowerCase())
  }

  // Handle batch search
  const handleBatchSearch = () => {
    if (selectedSites.length === 0) {
      toast.error('Please select at least one site')
      return
    }
    searchMultipleSites(selectedSites)
  }

  // Toggle site selection
  const toggleSiteSelection = (siteName: string) => {
    setSelectedSites(prev => 
      prev.includes(siteName) 
        ? prev.filter(s => s !== siteName)
        : [...prev, siteName]
    )
  }

  // Get status color
  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'WORKING':
        return 'text-success-400'
      case 'DOWN':
      case 'ERROR':
      case 'NOT_FOUND':
        return 'text-danger-400'
      case 'CHECKING':
        return 'text-yellow-400'
      default:
        return 'text-slate-400'
    }
  }

  // Get status icon
  const getStatusIcon = (status: string) => {
    switch (status.toUpperCase()) {
      case 'WORKING':
        return <CheckCircleIcon className="w-5 h-5 text-success-400" />
      case 'DOWN':
      case 'ERROR':
      case 'NOT_FOUND':
        return <XCircleIcon className="w-5 h-5 text-danger-400" />
      case 'CHECKING':
        return <ClockIcon className="w-5 h-5 text-yellow-400" />
      default:
        return <ClockIcon className="w-5 h-5 text-slate-400" />
    }
  }

  return (
    <div className="min-h-screen p-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold mb-4">
            <span className="text-gradient">Movie Links</span> Finder
          </h1>
          <p className="text-xl text-slate-300 max-w-2xl mx-auto">
            Find working links for popular movie streaming sites using advanced search technology
          </p>
        </div>

        {/* Single Site Search */}
        <div className="card mb-8">
          <h2 className="text-2xl font-semibold mb-6 flex items-center">
            <MagnifyingGlassIcon className="w-6 h-6 mr-2" />
            Search Single Site
          </h2>
          
          <form onSubmit={handleSingleSearch} className="flex gap-4">
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Enter site name (e.g., movierulz, moviezap)"
              className="input-field flex-1"
              disabled={isSearching}
            />
            <button
              type="submit"
              disabled={isSearching}
              className="btn-primary px-8 flex items-center"
            >
              {isSearching ? (
                <>
                  <div className="spinner mr-2"></div>
                  Searching...
                </>
              ) : (
                <>
                  <MagnifyingGlassIcon className="w-5 h-5 mr-2" />
                  Search
                </>
              )}
            </button>
          </form>
        </div>

        {/* Batch Search */}
        <div className="card mb-8">
          <h2 className="text-2xl font-semibold mb-6">Batch Search Popular Sites</h2>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
            {POPULAR_SITES.map((site) => (
              <label
                key={site}
                className={`flex items-center p-3 rounded-lg border cursor-pointer transition-all ${
                  selectedSites.includes(site)
                    ? 'border-primary-500 bg-primary-500/10'
                    : 'border-slate-600 hover:border-slate-500'
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedSites.includes(site)}
                  onChange={() => toggleSiteSelection(site)}
                  className="mr-3 text-primary-600"
                />
                <span className="capitalize">{site}</span>
              </label>
            ))}
          </div>

          <div className="flex gap-4">
            <button
              onClick={handleBatchSearch}
              disabled={isBatchSearching || selectedSites.length === 0}
              className="btn-primary flex items-center"
            >
              {isBatchSearching ? (
                <>
                  <div className="spinner mr-2"></div>
                  Searching {selectedSites.length} sites...
                </>
              ) : (
                <>
                  <MagnifyingGlassIcon className="w-5 h-5 mr-2" />
                  Search Selected ({selectedSites.length})
                </>
              )}
            </button>
            
            <button
              onClick={searchAllSites}
              disabled={isBatchSearching}
              className="btn-secondary flex items-center"
            >
              {isBatchSearching ? (
                <>
                  <div className="spinner mr-2"></div>
                  Searching all...
                </>
              ) : (
                'Search All Sites'
              )}
            </button>
          </div>
        </div>

        {/* Results */}
        {searchResults.length > 0 && (
          <div className="card">
            <h2 className="text-2xl font-semibold mb-6">Search Results</h2>
            
            <div className="grid gap-4">
              {searchResults.map((site, index) => (
                <div
                  key={index}
                  className="bg-slate-700/50 rounded-lg p-4 border border-slate-600"
                >
                  <div className="flex items-center justify-between mb-3">
                    <h3 className="text-xl font-semibold capitalize">{site.siteName}</h3>
                    <div className="flex items-center">
                      {getStatusIcon(site.status)}
                      <span className={`ml-2 font-medium ${getStatusColor(site.status)}`}>
                        {site.status}
                      </span>
                    </div>
                  </div>
                  
                  {site.currentWorkingUrl && (
                    <div className="mb-3">
                      <div className="flex items-center mb-2">
                        <LinkIcon className="w-4 h-4 mr-2 text-primary-400" />
                        <span className="text-sm text-slate-300">Working URL:</span>
                      </div>
                      <a
                        href={site.currentWorkingUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-primary-400 hover:text-primary-300 break-all"
                      >
                        {site.currentWorkingUrl}
                      </a>
                    </div>
                  )}
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-slate-400">
                    {site.lastChecked && (
                      <div>
                        <span className="font-medium">Last Checked:</span>
                        <br />
                        {new Date(site.lastChecked).toLocaleString()}
                      </div>
                    )}
                    
                    {site.responseTime && (
                      <div>
                        <span className="font-medium">Response Time:</span>
                        <br />
                        {site.responseTime}ms
                      </div>
                    )}
                    
                    {site.searchAliases && site.searchAliases.length > 0 && (
                      <div>
                        <span className="font-medium">Search Terms:</span>
                        <br />
                        {site.searchAliases.slice(0, 3).join(', ')}
                      </div>
                    )}
                  </div>
                  
                  {site.notes && (
                    <div className="mt-3 p-3 bg-slate-800 rounded text-sm">
                      <span className="font-medium text-slate-300">Notes:</span>
                      <br />
                      {site.notes}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="text-center mt-12 text-slate-400">
          <p>Powered by Browserless.io • Built with Next.js & Spring Boot</p>
        </div>
      </div>
    </div>
  )
}