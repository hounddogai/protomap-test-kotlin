package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered qualification metrics by code. */
interface TieredQualificationMetricStore {
    fun find(code: String): TieredQualificationMetric?
    fun save(item: TieredQualificationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredQualificationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredQualificationMetricStore(
    var items: MutableMap<String, TieredQualificationMetric>,
    var order: MutableList<String>,
) : TieredQualificationMetricStore {
    override fun find(code: String): TieredQualificationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredQualificationMetric) {
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

    override fun list(): MutableList<TieredQualificationMetric> {
        val out = mutableListOf<TieredQualificationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredQualificationMetricStore(): TieredQualificationMetricStore {
    return MemoryTieredQualificationMetricStore(
        mutableMapOf<String, TieredQualificationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredQualificationMetricStore(
    var inner: TieredQualificationMetricStore,
    var cache: TtlCache<TieredQualificationMetric>,
    var hits: Int,
    var misses: Int,
) : TieredQualificationMetricStore {
    override fun find(code: String): TieredQualificationMetric? {
        val hit = (cache.get(code) as TieredQualificationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredQualificationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredQualificationMetric> = inner.list()
}

fun newCachedTieredQualificationMetricStore(inner: TieredQualificationMetricStore, ttl: Int, clock: Clock): TieredQualificationMetricStore {
    return CachedTieredQualificationMetricStore(
        inner = inner,
        cache = newTtlCache<TieredQualificationMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered qualification metrics. Empty fields match all. */
data class TieredQualificationMetricQuery(
    var source: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredQualificationMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredQualificationMetric>): MutableList<TieredQualificationMetric> {
        val matched = filter(items) { candidate: TieredQualificationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredQualificationMetrics(store: TieredQualificationMetricStore, query: TieredQualificationMetricQuery): MutableList<TieredQualificationMetric> {
    return query.apply(store.list())
}
