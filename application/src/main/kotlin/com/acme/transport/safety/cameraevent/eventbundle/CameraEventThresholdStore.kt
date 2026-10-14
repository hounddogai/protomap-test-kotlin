package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event thresholds by code. */
interface CameraEventThresholdStore {
    fun find(code: String): CameraEventThreshold?
    fun save(item: CameraEventThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventThresholdStore(
    var items: MutableMap<String, CameraEventThreshold>,
    var order: MutableList<String>,
) : CameraEventThresholdStore {
    override fun find(code: String): CameraEventThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventThreshold) {
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

    override fun list(): MutableList<CameraEventThreshold> {
        val out = mutableListOf<CameraEventThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventThresholdStore(): CameraEventThresholdStore {
    return MemoryCameraEventThresholdStore(
        mutableMapOf<String, CameraEventThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventThresholdStore(
    var inner: CameraEventThresholdStore,
    var cache: TtlCache<CameraEventThreshold>,
    var hits: Int,
    var misses: Int,
) : CameraEventThresholdStore {
    override fun find(code: String): CameraEventThreshold? {
        val hit = (cache.get(code) as CameraEventThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventThreshold> = inner.list()
}

fun newCachedCameraEventThresholdStore(inner: CameraEventThresholdStore, ttl: Int, clock: Clock): CameraEventThresholdStore {
    return CachedCameraEventThresholdStore(
        inner = inner,
        cache = newTtlCache<CameraEventThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event thresholds. Empty fields match all. */
data class CameraEventThresholdQuery(
    var country: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventThreshold>): MutableList<CameraEventThreshold> {
        val matched = filter(items) { candidate: CameraEventThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventThresholds(store: CameraEventThresholdStore, query: CameraEventThresholdQuery): MutableList<CameraEventThreshold> {
    return query.apply(store.list())
}
