package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender metrics by code. */
interface TenderMetricStore {
    fun find(code: String): TenderMetric?
    fun save(item: TenderMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderMetricStore(
    var items: MutableMap<String, TenderMetric>,
    var order: MutableList<String>,
) : TenderMetricStore {
    override fun find(code: String): TenderMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderMetric) {
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

    override fun list(): MutableList<TenderMetric> {
        val out = mutableListOf<TenderMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderMetricStore(): TenderMetricStore {
    return MemoryTenderMetricStore(mutableMapOf<String, TenderMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderMetricStore(
    var inner: TenderMetricStore,
    var cache: TtlCache<TenderMetric>,
    var hits: Int,
    var misses: Int,
) : TenderMetricStore {
    override fun find(code: String): TenderMetric? {
        val hit = (cache.get(code) as TenderMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderMetric> = inner.list()
}

fun newCachedTenderMetricStore(inner: TenderMetricStore, ttl: Int, clock: Clock): TenderMetricStore {
    return CachedTenderMetricStore(
        inner = inner,
        cache = newTtlCache<TenderMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender metrics. Empty fields match all. */
data class TenderMetricQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderMetric>): MutableList<TenderMetric> {
        val matched = filter(items) { candidate: TenderMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderMetrics(store: TenderMetricStore, query: TenderMetricQuery): MutableList<TenderMetric> {
    return query.apply(store.list())
}
