package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient accessorial metrics by code. */
interface LenientAccessorialMetricStore {
    fun find(code: String): LenientAccessorialMetric?
    fun save(item: LenientAccessorialMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAccessorialMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAccessorialMetricStore(
    var items: MutableMap<String, LenientAccessorialMetric>,
    var order: MutableList<String>,
) : LenientAccessorialMetricStore {
    override fun find(code: String): LenientAccessorialMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAccessorialMetric) {
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

    override fun list(): MutableList<LenientAccessorialMetric> {
        val out = mutableListOf<LenientAccessorialMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAccessorialMetricStore(): LenientAccessorialMetricStore {
    return MemoryLenientAccessorialMetricStore(
        mutableMapOf<String, LenientAccessorialMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAccessorialMetricStore(
    var inner: LenientAccessorialMetricStore,
    var cache: TtlCache<LenientAccessorialMetric>,
    var hits: Int,
    var misses: Int,
) : LenientAccessorialMetricStore {
    override fun find(code: String): LenientAccessorialMetric? {
        val hit = (cache.get(code) as LenientAccessorialMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAccessorialMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAccessorialMetric> = inner.list()
}

fun newCachedLenientAccessorialMetricStore(inner: LenientAccessorialMetricStore, ttl: Int, clock: Clock): LenientAccessorialMetricStore {
    return CachedLenientAccessorialMetricStore(
        inner = inner,
        cache = newTtlCache<LenientAccessorialMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient accessorial metrics. Empty fields match all. */
data class LenientAccessorialMetricQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAccessorialMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAccessorialMetric>): MutableList<LenientAccessorialMetric> {
        val matched = filter(items) { candidate: LenientAccessorialMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAccessorialMetrics(store: LenientAccessorialMetricStore, query: LenientAccessorialMetricQuery): MutableList<LenientAccessorialMetric> {
    return query.apply(store.list())
}
