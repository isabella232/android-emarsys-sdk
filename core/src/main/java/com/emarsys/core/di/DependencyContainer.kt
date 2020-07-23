package com.emarsys.core.di

import android.os.Handler
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger


interface DependencyContainer {

    fun getCoreSdkHandler(): Handler

    fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog

    fun getCurrentActivityWatchdog(): CurrentActivityWatchdog

    fun getCoreSQLiteDatabase(): CoreSQLiteDatabase

    fun getDeviceInfo(): DeviceInfo

    fun getShardRepository(): Repository<ShardModel, SqlSpecification>

    fun getTimestampProvider(): TimestampProvider

    fun getUuidProvider(): UUIDProvider

    fun getLogShardTrigger(): Runnable

    fun getLogger(): Logger

    fun getRestClient(): RestClient

    fun getFileDownloader(): FileDownloader

    fun getKeyValueStore(): KeyValueStore

    val dependencies: MutableMap<String, Any?>


}

inline fun <reified T> generateDependencyName(key: String = ""): String {
    return T::class.java.name + key
}

inline fun <reified T> getDependency(key: String = ""): T {
    synchronized(DependencyContainer::class.java) {
        return DependencyInjection.getContainer<DependencyContainer>().dependencies[generateDependencyName<T>(key)] as T
    }
}

inline fun <reified T> getDependency(container: Map<String, Any?>, key: String = ""): T {
    synchronized(DependencyContainer::class.java) {
        return container[generateDependencyName<T>(key)] as T
    }
}

inline fun <reified T> addDependency(container: MutableMap<String, Any?>, dependency: T, key: String = "") {
    synchronized(DependencyContainer::class.java) {
        if (container[generateDependencyName<T>(key)] == null) {
            container[generateDependencyName<T>(key)] = dependency
        }
    }
}