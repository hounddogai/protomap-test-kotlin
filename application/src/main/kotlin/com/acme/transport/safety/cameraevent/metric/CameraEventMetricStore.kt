package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event metrics by code. */
interface CameraEventMetricStore {
    fun find(code: String): CameraEventMetric?
    fun save(item: CameraEventMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventMetricStore(
    var items: MutableMap<String, CameraEventMetric>,
    var order: MutableList<String>,
) : CameraEventMetricStore {
    override fun find(code: String): CameraEventMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventMetric) {
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

    override fun list(): MutableList<CameraEventMetric> {
        val out = mutableListOf<CameraEventMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventMetricStore(): CameraEventMetricStore {
    return MemoryCameraEventMetricStore(mutableMapOf<String, CameraEventMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventMetricStore(
    var inner: CameraEventMetricStore,
    var cache: TtlCache<CameraEventMetric>,
    var hits: Int,
    var misses: Int,
) : CameraEventMetricStore {
    override fun find(code: String): CameraEventMetric? {
        val hit = (cache.get(code) as CameraEventMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventMetric> = inner.list()
}

fun newCachedCameraEventMetricStore(inner: CameraEventMetricStore, ttl: Int, clock: Clock): CameraEventMetricStore {
    return CachedCameraEventMetricStore(
        inner = inner,
        cache = newTtlCache<CameraEventMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event metrics. Empty fields match all. */
data class CameraEventMetricQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventMetric>): MutableList<CameraEventMetric> {
        val matched = filter(items) { candidate: CameraEventMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventMetrics(store: CameraEventMetricStore, query: CameraEventMetricQuery): MutableList<CameraEventMetric> {
    return query.apply(store.list())
}
