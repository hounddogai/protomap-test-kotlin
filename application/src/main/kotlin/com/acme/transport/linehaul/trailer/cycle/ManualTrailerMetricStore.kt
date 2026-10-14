package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual trailer metrics by code. */
interface ManualTrailerMetricStore {
    fun find(code: String): ManualTrailerMetric?
    fun save(item: ManualTrailerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerMetricStore(
    var items: MutableMap<String, ManualTrailerMetric>,
    var order: MutableList<String>,
) : ManualTrailerMetricStore {
    override fun find(code: String): ManualTrailerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerMetric) {
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

    override fun list(): MutableList<ManualTrailerMetric> {
        val out = mutableListOf<ManualTrailerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerMetricStore(): ManualTrailerMetricStore {
    return MemoryManualTrailerMetricStore(mutableMapOf<String, ManualTrailerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerMetricStore(
    var inner: ManualTrailerMetricStore,
    var cache: TtlCache<ManualTrailerMetric>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerMetricStore {
    override fun find(code: String): ManualTrailerMetric? {
        val hit = (cache.get(code) as ManualTrailerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerMetric> = inner.list()
}

fun newCachedManualTrailerMetricStore(inner: ManualTrailerMetricStore, ttl: Int, clock: Clock): ManualTrailerMetricStore {
    return CachedManualTrailerMetricStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer metrics. Empty fields match all. */
data class ManualTrailerMetricQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerMetric>): MutableList<ManualTrailerMetric> {
        val matched = filter(items) { candidate: ManualTrailerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerMetrics(store: ManualTrailerMetricStore, query: ManualTrailerMetricQuery): MutableList<ManualTrailerMetric> {
    return query.apply(store.list())
}
