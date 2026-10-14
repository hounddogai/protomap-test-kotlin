package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual accessorial metrics by code. */
interface ManualAccessorialMetricStore {
    fun find(code: String): ManualAccessorialMetric?
    fun save(item: ManualAccessorialMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAccessorialMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAccessorialMetricStore(
    var items: MutableMap<String, ManualAccessorialMetric>,
    var order: MutableList<String>,
) : ManualAccessorialMetricStore {
    override fun find(code: String): ManualAccessorialMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAccessorialMetric) {
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

    override fun list(): MutableList<ManualAccessorialMetric> {
        val out = mutableListOf<ManualAccessorialMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAccessorialMetricStore(): ManualAccessorialMetricStore {
    return MemoryManualAccessorialMetricStore(
        mutableMapOf<String, ManualAccessorialMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAccessorialMetricStore(
    var inner: ManualAccessorialMetricStore,
    var cache: TtlCache<ManualAccessorialMetric>,
    var hits: Int,
    var misses: Int,
) : ManualAccessorialMetricStore {
    override fun find(code: String): ManualAccessorialMetric? {
        val hit = (cache.get(code) as ManualAccessorialMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAccessorialMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAccessorialMetric> = inner.list()
}

fun newCachedManualAccessorialMetricStore(inner: ManualAccessorialMetricStore, ttl: Int, clock: Clock): ManualAccessorialMetricStore {
    return CachedManualAccessorialMetricStore(
        inner = inner,
        cache = newTtlCache<ManualAccessorialMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual accessorial metrics. Empty fields match all. */
data class ManualAccessorialMetricQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAccessorialMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAccessorialMetric>): MutableList<ManualAccessorialMetric> {
        val matched = filter(items) { candidate: ManualAccessorialMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAccessorialMetrics(store: ManualAccessorialMetricStore, query: ManualAccessorialMetricQuery): MutableList<ManualAccessorialMetric> {
    return query.apply(store.list())
}
