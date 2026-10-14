package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial metrics by code. */
interface AccessorialMetricStore {
    fun find(code: String): AccessorialMetric?
    fun save(item: AccessorialMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialMetricStore(
    var items: MutableMap<String, AccessorialMetric>,
    var order: MutableList<String>,
) : AccessorialMetricStore {
    override fun find(code: String): AccessorialMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialMetric) {
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

    override fun list(): MutableList<AccessorialMetric> {
        val out = mutableListOf<AccessorialMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialMetricStore(): AccessorialMetricStore {
    return MemoryAccessorialMetricStore(mutableMapOf<String, AccessorialMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialMetricStore(
    var inner: AccessorialMetricStore,
    var cache: TtlCache<AccessorialMetric>,
    var hits: Int,
    var misses: Int,
) : AccessorialMetricStore {
    override fun find(code: String): AccessorialMetric? {
        val hit = (cache.get(code) as AccessorialMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialMetric> = inner.list()
}

fun newCachedAccessorialMetricStore(inner: AccessorialMetricStore, ttl: Int, clock: Clock): AccessorialMetricStore {
    return CachedAccessorialMetricStore(
        inner = inner,
        cache = newTtlCache<AccessorialMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial metrics. Empty fields match all. */
data class AccessorialMetricQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AccessorialMetric>): MutableList<AccessorialMetric> {
        val matched = filter(items) { candidate: AccessorialMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialMetrics(store: AccessorialMetricStore, query: AccessorialMetricQuery): MutableList<AccessorialMetric> {
    return query.apply(store.list())
}
