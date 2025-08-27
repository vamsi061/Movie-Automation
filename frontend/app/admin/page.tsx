'use client'

import { useState, useEffect } from 'react'
import { 
  ChartBarIcon, 
  CogIcon, 
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  ArrowPathIcon,
  PlusIcon,
  TrashIcon,
  PencilIcon,
  BellIcon,
  DocumentTextIcon,
  EyeIcon
} from '@heroicons/react/24/outline'
import { toast } from 'react-hot-toast'
import axios from 'axios'

interface MovieSite {
  id: number
  siteName: string
  currentWorkingUrl?: string
  status: string
  lastChecked?: string
  lastUpdated?: string
  responseTime?: number
  isActive?: boolean
  notes?: string
}

interface SystemHealth {
  status: string
  totalSites: number
  workingSites: number
  downSites: number
  uptimePercentage: number
  averageResponseTime: number
  lastChecked: string
}

interface DashboardData {
  healthStatus: SystemHealth
  statistics: any
  recentActivity: MovieSite[]
}

export default function AdminDashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null)
  const [sites, setSites] = useState<MovieSite[]>([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('dashboard')
  const [selectedSite, setSelectedSite] = useState<MovieSite | null>(null)
  const [showAddSiteModal, setShowAddSiteModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [newSiteName, setNewSiteName] = useState('')
  const [editData, setEditData] = useState<any>({})
  const [refreshing, setRefreshing] = useState(false)
  const [logs, setLogs] = useState<any[]>([])

  const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

  // Load dashboard data
  const loadDashboard = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/admin/dashboard`)
      setDashboardData(response.data)
    } catch (error) {
      console.error('Error loading dashboard:', error)
      toast.error('Failed to load dashboard data')
    }
  }

  // Load sites
  const loadSites = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/admin/sites?size=100`)
      setSites(response.data.sites)
    } catch (error) {
      console.error('Error loading sites:', error)
      toast.error('Failed to load sites')
    }
  }

  // Load activity logs
  const loadLogs = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/admin/logs?size=50`)
      setLogs(response.data.logs)
    } catch (error) {
      console.error('Error loading logs:', error)
      toast.error('Failed to load activity logs')
    }
  }

  // Initial load
  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      await Promise.all([loadDashboard(), loadSites(), loadLogs()])
      setLoading(false)
    }
    loadData()
  }, [])

  // Refresh specific site
  const refreshSite = async (siteId: number) => {
    try {
      setRefreshing(true)
      await axios.post(`${API_URL}/api/admin/sites/${siteId}/refresh`)
      toast.success('Site refresh started')
      await loadSites()
    } catch (error) {
      toast.error('Failed to refresh site')
    } finally {
      setRefreshing(false)
    }
  }

  // Refresh all sites
  const refreshAllSites = async () => {
    try {
      setRefreshing(true)
      await axios.post(`${API_URL}/api/admin/sites/refresh-all`)
      toast.success('Refresh started for all sites')
      setTimeout(() => loadSites(), 5000) // Reload after 5 seconds
    } catch (error) {
      toast.error('Failed to start refresh')
    } finally {
      setRefreshing(false)
    }
  }

  // Add new site
  const addNewSite = async () => {
    if (!newSiteName.trim()) {
      toast.error('Please enter a site name')
      return
    }

    try {
      await axios.post(`${API_URL}/api/admin/sites`, { siteName: newSiteName })
      toast.success('Site added successfully')
      setNewSiteName('')
      setShowAddSiteModal(false)
      await loadSites()
    } catch (error: any) {
      toast.error(error.response?.data?.error || 'Failed to add site')
    }
  }

  // Update site
  const updateSite = async () => {
    if (!selectedSite) return

    try {
      await axios.put(`${API_URL}/api/admin/sites/${selectedSite.id}`, editData)
      toast.success('Site updated successfully')
      setShowEditModal(false)
      setSelectedSite(null)
      setEditData({})
      await loadSites()
    } catch (error) {
      toast.error('Failed to update site')
    }
  }

  // Delete site
  const deleteSite = async (siteId: number) => {
    if (!confirm('Are you sure you want to delete this site?')) return

    try {
      await axios.delete(`${API_URL}/api/admin/sites/${siteId}`)
      toast.success('Site deleted successfully')
      await loadSites()
    } catch (error) {
      toast.error('Failed to delete site')
    }
  }

  // Send test notification
  const sendTestNotification = async () => {
    try {
      await axios.post(`${API_URL}/api/admin/notifications/test`)
      toast.success('Test notification sent')
    } catch (error) {
      toast.error('Failed to send test notification')
    }
  }

  // Trigger health check
  const triggerHealthCheck = async () => {
    try {
      await axios.post(`${API_URL}/api/admin/monitoring/health-check`)
      toast.success('Health check started')
      setTimeout(() => loadDashboard(), 5000)
    } catch (error) {
      toast.error('Failed to start health check')
    }
  }

  // Get status color
  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'WORKING': return 'text-green-400'
      case 'DOWN': case 'ERROR': case 'NOT_FOUND': return 'text-red-400'
      case 'CHECKING': return 'text-yellow-400'
      default: return 'text-gray-400'
    }
  }

  // Get health status color
  const getHealthColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'HEALTHY': return 'text-green-400'
      case 'DEGRADED': return 'text-yellow-400'
      case 'CRITICAL': return 'text-red-400'
      default: return 'text-gray-400'
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="spinner mx-auto mb-4"></div>
          <p className="text-white">Loading admin dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gradient mb-2">Admin Dashboard</h1>
          <p className="text-slate-300">Monitor and manage movie site links</p>
        </div>

        {/* Navigation Tabs */}
        <div className="flex space-x-1 mb-8 bg-slate-800/50 p-1 rounded-lg">
          {[
            { id: 'dashboard', label: 'Dashboard', icon: ChartBarIcon },
            { id: 'sites', label: 'Sites', icon: CogIcon },
            { id: 'monitoring', label: 'Monitoring', icon: ExclamationTriangleIcon },
            { id: 'logs', label: 'Activity Logs', icon: DocumentTextIcon },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center px-4 py-2 rounded-md transition-colors ${
                activeTab === tab.id
                  ? 'bg-primary-600 text-white'
                  : 'text-slate-300 hover:text-white hover:bg-slate-700'
              }`}
            >
              <tab.icon className="w-5 h-5 mr-2" />
              {tab.label}
            </button>
          ))}
        </div>

        {/* Dashboard Tab */}
        {activeTab === 'dashboard' && dashboardData && (
          <div className="space-y-6">
            {/* System Health Overview */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-slate-400 text-sm">System Status</p>
                    <p className={`text-2xl font-bold ${getHealthColor(dashboardData.healthStatus.status)}`}>
                      {dashboardData.healthStatus.status}
                    </p>
                  </div>
                  <div className={`p-3 rounded-full ${
                    dashboardData.healthStatus.status === 'HEALTHY' ? 'bg-green-500/20' :
                    dashboardData.healthStatus.status === 'DEGRADED' ? 'bg-yellow-500/20' : 'bg-red-500/20'
                  }`}>
                    {dashboardData.healthStatus.status === 'HEALTHY' ? 
                      <CheckCircleIcon className="w-8 h-8 text-green-400" /> :
                      <ExclamationTriangleIcon className="w-8 h-8 text-yellow-400" />
                    }
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-slate-400 text-sm">Working Sites</p>
                    <p className="text-2xl font-bold text-green-400">
                      {dashboardData.healthStatus.workingSites}
                    </p>
                  </div>
                  <CheckCircleIcon className="w-8 h-8 text-green-400" />
                </div>
              </div>

              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-slate-400 text-sm">Down Sites</p>
                    <p className="text-2xl font-bold text-red-400">
                      {dashboardData.healthStatus.downSites}
                    </p>
                  </div>
                  <XCircleIcon className="w-8 h-8 text-red-400" />
                </div>
              </div>

              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-slate-400 text-sm">Uptime</p>
                    <p className="text-2xl font-bold text-primary-400">
                      {dashboardData.healthStatus.uptimePercentage.toFixed(1)}%
                    </p>
                  </div>
                  <ChartBarIcon className="w-8 h-8 text-primary-400" />
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="card">
              <h3 className="text-xl font-semibold mb-4">Quick Actions</h3>
              <div className="flex flex-wrap gap-4">
                <button
                  onClick={refreshAllSites}
                  disabled={refreshing}
                  className="btn-primary flex items-center"
                >
                  <ArrowPathIcon className="w-5 h-5 mr-2" />
                  {refreshing ? 'Refreshing...' : 'Refresh All Sites'}
                </button>
                
                <button
                  onClick={triggerHealthCheck}
                  className="btn-secondary flex items-center"
                >
                  <ExclamationTriangleIcon className="w-5 h-5 mr-2" />
                  Run Health Check
                </button>
                
                <button
                  onClick={sendTestNotification}
                  className="btn-secondary flex items-center"
                >
                  <BellIcon className="w-5 h-5 mr-2" />
                  Test Notifications
                </button>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="card">
              <h3 className="text-xl font-semibold mb-4">Recent Activity</h3>
              <div className="space-y-3">
                {dashboardData.recentActivity.slice(0, 5).map((site, index) => (
                  <div key={index} className="flex items-center justify-between p-3 bg-slate-700/50 rounded-lg">
                    <div>
                      <p className="font-medium">{site.siteName}</p>
                      <p className="text-sm text-slate-400">
                        {site.lastUpdated ? new Date(site.lastUpdated).toLocaleString() : 'Never'}
                      </p>
                    </div>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(site.status)}`}>
                      {site.status}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Sites Management Tab */}
        {activeTab === 'sites' && (
          <div className="space-y-6">
            {/* Sites Header */}
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-semibold">Sites Management</h2>
              <button
                onClick={() => setShowAddSiteModal(true)}
                className="btn-primary flex items-center"
              >
                <PlusIcon className="w-5 h-5 mr-2" />
                Add New Site
              </button>
            </div>

            {/* Sites Table */}
            <div className="card overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-slate-700/50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                        Site Name
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                        Current URL
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                        Last Checked
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-700">
                    {sites.map((site) => (
                      <tr key={site.id} className="hover:bg-slate-700/25">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="font-medium text-white">{site.siteName}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            site.status === 'WORKING' ? 'bg-green-100 text-green-800' :
                            site.status === 'DOWN' ? 'bg-red-100 text-red-800' :
                            'bg-yellow-100 text-yellow-800'
                          }`}>
                            {site.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-slate-300 max-w-xs truncate">
                            {site.currentWorkingUrl || 'N/A'}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-300">
                          {site.lastChecked ? new Date(site.lastChecked).toLocaleString() : 'Never'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex space-x-2">
                            <button
                              onClick={() => refreshSite(site.id)}
                              className="text-primary-400 hover:text-primary-300"
                              title="Refresh"
                            >
                              <ArrowPathIcon className="w-5 h-5" />
                            </button>
                            <button
                              onClick={() => {
                                setSelectedSite(site)
                                setEditData({
                                  currentWorkingUrl: site.currentWorkingUrl,
                                  status: site.status,
                                  isActive: site.isActive,
                                  notes: site.notes
                                })
                                setShowEditModal(true)
                              }}
                              className="text-yellow-400 hover:text-yellow-300"
                              title="Edit"
                            >
                              <PencilIcon className="w-5 h-5" />
                            </button>
                            <button
                              onClick={() => deleteSite(site.id)}
                              className="text-red-400 hover:text-red-300"
                              title="Delete"
                            >
                              <TrashIcon className="w-5 h-5" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Monitoring Tab */}
        {activeTab === 'monitoring' && dashboardData && (
          <div className="space-y-6">
            <h2 className="text-2xl font-semibold">System Monitoring</h2>
            
            {/* Health Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="card">
                <h3 className="text-lg font-semibold mb-4">Response Time</h3>
                <div className="text-3xl font-bold text-primary-400">
                  {dashboardData.healthStatus.averageResponseTime.toFixed(0)}ms
                </div>
                <p className="text-slate-400 text-sm">Average response time</p>
              </div>
              
              <div className="card">
                <h3 className="text-lg font-semibold mb-4">Uptime</h3>
                <div className="text-3xl font-bold text-green-400">
                  {dashboardData.healthStatus.uptimePercentage.toFixed(1)}%
                </div>
                <p className="text-slate-400 text-sm">System uptime</p>
              </div>
              
              <div className="card">
                <h3 className="text-lg font-semibold mb-4">Last Check</h3>
                <div className="text-lg text-slate-300">
                  {dashboardData.healthStatus.lastChecked ? 
                    new Date(dashboardData.healthStatus.lastChecked).toLocaleString() : 
                    'Never'
                  }
                </div>
                <p className="text-slate-400 text-sm">Last health check</p>
              </div>
            </div>

            {/* Monitoring Actions */}
            <div className="card">
              <h3 className="text-xl font-semibold mb-4">Monitoring Actions</h3>
              <div className="flex flex-wrap gap-4">
                <button
                  onClick={triggerHealthCheck}
                  className="btn-primary flex items-center"
                >
                  <ExclamationTriangleIcon className="w-5 h-5 mr-2" />
                  Run Health Check
                </button>
                
                <button
                  onClick={sendTestNotification}
                  className="btn-secondary flex items-center"
                >
                  <BellIcon className="w-5 h-5 mr-2" />
                  Test Notifications
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Activity Logs Tab */}
        {activeTab === 'logs' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-semibold">Activity Logs</h2>
            
            <div className="card">
              <div className="space-y-3">
                {logs.map((log, index) => (
                  <div key={index} className="flex items-center justify-between p-3 bg-slate-700/50 rounded-lg">
                    <div className="flex items-center space-x-4">
                      <ClockIcon className="w-5 h-5 text-slate-400" />
                      <div>
                        <p className="font-medium">{log.action}</p>
                        <p className="text-sm text-slate-400">
                          {log.siteName} - {log.status}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-slate-300">
                        {new Date(log.timestamp).toLocaleString()}
                      </p>
                      {log.url && log.url !== 'N/A' && (
                        <p className="text-xs text-slate-400 max-w-xs truncate">
                          {log.url}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Add Site Modal */}
      {showAddSiteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-lg p-6 w-full max-w-md">
            <h3 className="text-xl font-semibold mb-4">Add New Site</h3>
            <input
              type="text"
              value={newSiteName}
              onChange={(e) => setNewSiteName(e.target.value)}
              placeholder="Enter site name (e.g., movierulz)"
              className="input-field mb-4"
            />
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setShowAddSiteModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={addNewSite}
                className="btn-primary"
              >
                Add Site
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Site Modal */}
      {showEditModal && selectedSite && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-lg p-6 w-full max-w-md">
            <h3 className="text-xl font-semibold mb-4">Edit Site: {selectedSite.siteName}</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1">
                  Current Working URL
                </label>
                <input
                  type="text"
                  value={editData.currentWorkingUrl || ''}
                  onChange={(e) => setEditData({...editData, currentWorkingUrl: e.target.value})}
                  className="input-field"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1">
                  Status
                </label>
                <select
                  value={editData.status || ''}
                  onChange={(e) => setEditData({...editData, status: e.target.value})}
                  className="input-field"
                >
                  <option value="WORKING">WORKING</option>
                  <option value="DOWN">DOWN</option>
                  <option value="ERROR">ERROR</option>
                  <option value="CHECKING">CHECKING</option>
                </select>
              </div>
              
              <div>
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={editData.isActive || false}
                    onChange={(e) => setEditData({...editData, isActive: e.target.checked})}
                    className="mr-2"
                  />
                  <span className="text-slate-300">Active</span>
                </label>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1">
                  Notes
                </label>
                <textarea
                  value={editData.notes || ''}
                  onChange={(e) => setEditData({...editData, notes: e.target.value})}
                  className="input-field"
                  rows={3}
                />
              </div>
            </div>
            
            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setShowEditModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={updateSite}
                className="btn-primary"
              >
                Update Site
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}