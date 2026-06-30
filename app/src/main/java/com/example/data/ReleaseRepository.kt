package com.example.data

import kotlinx.coroutines.flow.Flow

class ReleaseRepository(
    private val releaseDao: ReleaseDao,
    private val recentActivityDao: RecentActivityDao
) {
    val allReleases: Flow<List<Release>> = releaseDao.getAllReleases()
    val recentActivities: Flow<List<RecentActivity>> = recentActivityDao.getRecentActivities()

    suspend fun insert(release: Release): Long {
        return releaseDao.insertRelease(release)
    }

    suspend fun delete(release: Release) {
        releaseDao.deleteRelease(release)
    }

    suspend fun updateStatus(id: Int, status: String) {
        releaseDao.updateReleaseStatus(id, status)
    }

    suspend fun getReleaseById(id: Int): Release? {
        return releaseDao.getReleaseById(id)
    }

    suspend fun insertActivity(activity: RecentActivity) {
        recentActivityDao.insertActivity(activity)
    }

    suspend fun clearActivities() {
        recentActivityDao.clearAllActivities()
    }
}
