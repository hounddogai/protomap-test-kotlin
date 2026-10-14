package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation metrics by code. */
interface SortationMetricStore {
    fun find(code: String): SortationMetric?
    fun save(item: SortationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationMetricStore(
    var items: MutableMap<String, SortationMetric>,
    var order: MutableList<String>,
) : SortationMetricStore {
    override fun find(code: String): SortationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationMetric) {
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

    override fun list(): MutableList<SortationMetric> {
        val out = mutableListOf<SortationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationMetricStore(): SortationMetricStore {
    return MemorySortationMetricStore(mutableMapOf<String, SortationMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationMetricStore(
    var inner: SortationMetricStore,
    var cache: TtlCache<SortationMetric>,
    var hits: Int,
    var misses: Int,
) : SortationMetricStore {
    override fun find(code: String): SortationMetric? {
        val hit = (cache.get(code) as SortationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationMetric> = inner.list()
}

fun newCachedSortationMetricStore(inner: SortationMetricStore, ttl: Int, clock: Clock): SortationMetricStore {
    return CachedSortationMetricStore(
        inner = inner,
        cache = newTtlCache<SortationMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation metrics. Empty fields match all. */
data class SortationMetricQuery(
    var unit: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationMetric>): MutableList<SortationMetric> {
        val matched = filter(items) { candidate: SortationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationMetrics(store: SortationMetricStore, query: SortationMetricQuery): MutableList<SortationMetric> {
    return query.apply(store.list())
}
