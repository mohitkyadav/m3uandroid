package com.m3u.data.repository.impl

import com.m3u.data.dao.LiveDao
import com.m3u.data.entity.Live
import com.m3u.data.repository.LiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LiveRepositoryImpl @Inject constructor(
    private val liveDao: LiveDao
) : LiveRepository {
    override fun observe(id: Int): Flow<Live?> = try {
        liveDao.observeById(id)
    } catch (e: Exception) {
        flow {}
    }

    override fun observeAllLives(): Flow<List<Live>> = try {
        liveDao.observeAll()
    } catch (e: Exception) {
        flow {}
    }

    override suspend fun getBySubscriptionUrl(subscriptionUrl: String): List<Live> = try {
        liveDao.getBySubscriptionId(subscriptionUrl)
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun getByUrl(url: String): Live? = try {
        liveDao.getByUrl(url)
    } catch (e: Exception) {
        null
    }

    override suspend fun setFavouriteLive(id: Int, target: Boolean) = try {
        liveDao.setFavouriteLive(id, target)
    } catch (_: Exception) {
    }
}