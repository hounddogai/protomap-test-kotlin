package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict camera event metrics by code. */
interface StrictCameraEventMetricStore {
    fun find(code: String): StrictCameraEventMetric?
    fun save(item: StrictCameraEventMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCameraEventMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCameraEventMetricStore(
    var items: MutableMap<String, StrictCameraEventMetric>,
    var order: MutableList<String>,
) : StrictCameraEventMetricStore {
    override fun find(code: String): StrictCameraEventMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCameraEventMetric) {
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

    override fun list(): MutableList<StrictCameraEventMetric> {
        val out = mutableListOf<StrictCameraEventMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCameraEventMetricStore(): StrictCameraEventMetricStore {
    return MemoryStrictCameraEventMetricStore(
        mutableMapOf<String, StrictCameraEventMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCameraEventMetricStore(
    var inner: StrictCameraEventMetricStore,
    var cache: TtlCache<StrictCameraEventMetric>,
    var hits: Int,
    var misses: Int,
) : StrictCameraEventMetricStore {
    override fun find(code: String): StrictCameraEventMetric? {
        val hit = (cache.get(code) as StrictCameraEventMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCameraEventMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCameraEventMetric> = inner.list()
}

fun newCachedStrictCameraEventMetricStore(inner: StrictCameraEventMetricStore, ttl: Int, clock: Clock): StrictCameraEventMetricStore {
    return CachedStrictCameraEventMetricStore(
        inner = inner,
        cache = newTtlCache<StrictCameraEventMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict camera event metrics. Empty fields match all. */
data class StrictCameraEventMetricQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCameraEventMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCameraEventMetric>): MutableList<StrictCameraEventMetric> {
        val matched = filter(items) { candidate: StrictCameraEventMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCameraEventMetrics(store: StrictCameraEventMetricStore, query: StrictCameraEventMetricQuery): MutableList<StrictCameraEventMetric> {
    return query.apply(store.list())
}
