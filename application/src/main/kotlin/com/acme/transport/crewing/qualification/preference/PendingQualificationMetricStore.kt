package com.acme.transport.crewing.qualification.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending qualification metrics by code. */
interface PendingQualificationMetricStore {
    fun find(code: String): PendingQualificationMetric?
    fun save(item: PendingQualificationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingQualificationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingQualificationMetricStore(
    var items: MutableMap<String, PendingQualificationMetric>,
    var order: MutableList<String>,
) : PendingQualificationMetricStore {
    override fun find(code: String): PendingQualificationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingQualificationMetric) {
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

    override fun list(): MutableList<PendingQualificationMetric> {
        val out = mutableListOf<PendingQualificationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingQualificationMetricStore(): PendingQualificationMetricStore {
    return MemoryPendingQualificationMetricStore(
        mutableMapOf<String, PendingQualificationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingQualificationMetricStore(
    var inner: PendingQualificationMetricStore,
    var cache: TtlCache<PendingQualificationMetric>,
    var hits: Int,
    var misses: Int,
) : PendingQualificationMetricStore {
    override fun find(code: String): PendingQualificationMetric? {
        val hit = (cache.get(code) as PendingQualificationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingQualificationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingQualificationMetric> = inner.list()
}

fun newCachedPendingQualificationMetricStore(inner: PendingQualificationMetricStore, ttl: Int, clock: Clock): PendingQualificationMetricStore {
    return CachedPendingQualificationMetricStore(
        inner = inner,
        cache = newTtlCache<PendingQualificationMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending qualification metrics. Empty fields match all. */
data class PendingQualificationMetricQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingQualificationMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingQualificationMetric>): MutableList<PendingQualificationMetric> {
        val matched = filter(items) { candidate: PendingQualificationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingQualificationMetrics(store: PendingQualificationMetricStore, query: PendingQualificationMetricQuery): MutableList<PendingQualificationMetric> {
    return query.apply(store.list())
}
