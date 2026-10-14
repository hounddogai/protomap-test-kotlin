package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event variances by code. */
interface CameraEventVarianceStore {
    fun find(code: String): CameraEventVariance?
    fun save(item: CameraEventVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventVarianceStore(
    var items: MutableMap<String, CameraEventVariance>,
    var order: MutableList<String>,
) : CameraEventVarianceStore {
    override fun find(code: String): CameraEventVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventVariance) {
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

    override fun list(): MutableList<CameraEventVariance> {
        val out = mutableListOf<CameraEventVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventVarianceStore(): CameraEventVarianceStore {
    return MemoryCameraEventVarianceStore(mutableMapOf<String, CameraEventVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventVarianceStore(
    var inner: CameraEventVarianceStore,
    var cache: TtlCache<CameraEventVariance>,
    var hits: Int,
    var misses: Int,
) : CameraEventVarianceStore {
    override fun find(code: String): CameraEventVariance? {
        val hit = (cache.get(code) as CameraEventVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventVariance> = inner.list()
}

fun newCachedCameraEventVarianceStore(inner: CameraEventVarianceStore, ttl: Int, clock: Clock): CameraEventVarianceStore {
    return CachedCameraEventVarianceStore(
        inner = inner,
        cache = newTtlCache<CameraEventVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event variances. Empty fields match all. */
data class CameraEventVarianceQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventVariance>): MutableList<CameraEventVariance> {
        val matched = filter(items) { candidate: CameraEventVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventVariances(store: CameraEventVarianceStore, query: CameraEventVarianceQuery): MutableList<CameraEventVariance> {
    return query.apply(store.list())
}
