package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area checkpoints by code. */
interface ServiceAreaCheckpointStore {
    fun find(code: String): ServiceAreaCheckpoint?
    fun save(item: ServiceAreaCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaCheckpointStore(
    var items: MutableMap<String, ServiceAreaCheckpoint>,
    var order: MutableList<String>,
) : ServiceAreaCheckpointStore {
    override fun find(code: String): ServiceAreaCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaCheckpoint) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ServiceAreaCheckpoint> {
        val out = mutableListOf<ServiceAreaCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaCheckpointStore(): ServiceAreaCheckpointStore {
    return MemoryServiceAreaCheckpointStore(
        mutableMapOf<String, ServiceAreaCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaCheckpointStore(
    var inner: ServiceAreaCheckpointStore,
    var cache: TtlCache<ServiceAreaCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaCheckpointStore {
    override fun find(code: String): ServiceAreaCheckpoint? {
        val hit = (cache.get(code) as ServiceAreaCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaCheckpoint> = inner.list()
}

fun newCachedServiceAreaCheckpointStore(inner: ServiceAreaCheckpointStore, ttl: Int, clock: Clock): ServiceAreaCheckpointStore {
    return CachedServiceAreaCheckpointStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area checkpoints. Empty fields match all. */
data class ServiceAreaCheckpointQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaCheckpoint>): MutableList<ServiceAreaCheckpoint> {
        val matched = filter(items) { candidate: ServiceAreaCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaCheckpoints(store: ServiceAreaCheckpointStore, query: ServiceAreaCheckpointQuery): MutableList<ServiceAreaCheckpoint> {
    return query.apply(store.list())
}
