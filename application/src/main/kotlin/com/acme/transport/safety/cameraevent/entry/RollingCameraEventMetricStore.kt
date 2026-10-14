package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling camera event metrics by code. */
interface RollingCameraEventMetricStore {
    fun find(code: String): RollingCameraEventMetric?
    fun save(item: RollingCameraEventMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCameraEventMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCameraEventMetricStore(
    var items: MutableMap<String, RollingCameraEventMetric>,
    var order: MutableList<String>,
) : RollingCameraEventMetricStore {
    override fun find(code: String): RollingCameraEventMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCameraEventMetric) {
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

    override fun list(): MutableList<RollingCameraEventMetric> {
        val out = mutableListOf<RollingCameraEventMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCameraEventMetricStore(): RollingCameraEventMetricStore {
    return MemoryRollingCameraEventMetricStore(
        mutableMapOf<String, RollingCameraEventMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCameraEventMetricStore(
    var inner: RollingCameraEventMetricStore,
    var cache: TtlCache<RollingCameraEventMetric>,
    var hits: Int,
    var misses: Int,
) : RollingCameraEventMetricStore {
    override fun find(code: String): RollingCameraEventMetric? {
        val hit = (cache.get(code) as RollingCameraEventMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCameraEventMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCameraEventMetric> = inner.list()
}

fun newCachedRollingCameraEventMetricStore(inner: RollingCameraEventMetricStore, ttl: Int, clock: Clock): RollingCameraEventMetricStore {
    return CachedRollingCameraEventMetricStore(
        inner = inner,
        cache = newTtlCache<RollingCameraEventMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling camera event metrics. Empty fields match all. */
data class RollingCameraEventMetricQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCameraEventMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCameraEventMetric>): MutableList<RollingCameraEventMetric> {
        val matched = filter(items) { candidate: RollingCameraEventMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCameraEventMetrics(store: RollingCameraEventMetricStore, query: RollingCameraEventMetricQuery): MutableList<RollingCameraEventMetric> {
    return query.apply(store.list())
}
