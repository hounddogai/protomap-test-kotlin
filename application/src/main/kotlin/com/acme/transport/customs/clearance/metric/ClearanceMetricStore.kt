package com.acme.transport.customs.clearance.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance metrics by code. */
interface ClearanceMetricStore {
    fun find(code: String): ClearanceMetric?
    fun save(item: ClearanceMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceMetricStore(
    var items: MutableMap<String, ClearanceMetric>,
    var order: MutableList<String>,
) : ClearanceMetricStore {
    override fun find(code: String): ClearanceMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceMetric) {
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

    override fun list(): MutableList<ClearanceMetric> {
        val out = mutableListOf<ClearanceMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceMetricStore(): ClearanceMetricStore {
    return MemoryClearanceMetricStore(mutableMapOf<String, ClearanceMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceMetricStore(
    var inner: ClearanceMetricStore,
    var cache: TtlCache<ClearanceMetric>,
    var hits: Int,
    var misses: Int,
) : ClearanceMetricStore {
    override fun find(code: String): ClearanceMetric? {
        val hit = (cache.get(code) as ClearanceMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceMetric> = inner.list()
}

fun newCachedClearanceMetricStore(inner: ClearanceMetricStore, ttl: Int, clock: Clock): ClearanceMetricStore {
    return CachedClearanceMetricStore(
        inner = inner,
        cache = newTtlCache<ClearanceMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance metrics. Empty fields match all. */
data class ClearanceMetricQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ClearanceMetric>): MutableList<ClearanceMetric> {
        val matched = filter(items) { candidate: ClearanceMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceMetrics(store: ClearanceMetricStore, query: ClearanceMetricQuery): MutableList<ClearanceMetric> {
    return query.apply(store.list())
}
