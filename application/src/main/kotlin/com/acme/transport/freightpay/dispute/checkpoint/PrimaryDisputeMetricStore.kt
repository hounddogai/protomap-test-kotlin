package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary dispute metrics by code. */
interface PrimaryDisputeMetricStore {
    fun find(code: String): PrimaryDisputeMetric?
    fun save(item: PrimaryDisputeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDisputeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDisputeMetricStore(
    var items: MutableMap<String, PrimaryDisputeMetric>,
    var order: MutableList<String>,
) : PrimaryDisputeMetricStore {
    override fun find(code: String): PrimaryDisputeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDisputeMetric) {
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

    override fun list(): MutableList<PrimaryDisputeMetric> {
        val out = mutableListOf<PrimaryDisputeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDisputeMetricStore(): PrimaryDisputeMetricStore {
    return MemoryPrimaryDisputeMetricStore(
        mutableMapOf<String, PrimaryDisputeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDisputeMetricStore(
    var inner: PrimaryDisputeMetricStore,
    var cache: TtlCache<PrimaryDisputeMetric>,
    var hits: Int,
    var misses: Int,
) : PrimaryDisputeMetricStore {
    override fun find(code: String): PrimaryDisputeMetric? {
        val hit = (cache.get(code) as PrimaryDisputeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDisputeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDisputeMetric> = inner.list()
}

fun newCachedPrimaryDisputeMetricStore(inner: PrimaryDisputeMetricStore, ttl: Int, clock: Clock): PrimaryDisputeMetricStore {
    return CachedPrimaryDisputeMetricStore(
        inner = inner,
        cache = newTtlCache<PrimaryDisputeMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary dispute metrics. Empty fields match all. */
data class PrimaryDisputeMetricQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDisputeMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDisputeMetric>): MutableList<PrimaryDisputeMetric> {
        val matched = filter(items) { candidate: PrimaryDisputeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDisputeMetrics(store: PrimaryDisputeMetricStore, query: PrimaryDisputeMetricQuery): MutableList<PrimaryDisputeMetric> {
    return query.apply(store.list())
}
