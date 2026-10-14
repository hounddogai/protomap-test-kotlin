package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute metrics by code. */
interface LenientDisputeMetricStore {
    fun find(code: String): LenientDisputeMetric?
    fun save(item: LenientDisputeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeMetricStore(
    var items: MutableMap<String, LenientDisputeMetric>,
    var order: MutableList<String>,
) : LenientDisputeMetricStore {
    override fun find(code: String): LenientDisputeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeMetric) {
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

    override fun list(): MutableList<LenientDisputeMetric> {
        val out = mutableListOf<LenientDisputeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeMetricStore(): LenientDisputeMetricStore {
    return MemoryLenientDisputeMetricStore(
        mutableMapOf<String, LenientDisputeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeMetricStore(
    var inner: LenientDisputeMetricStore,
    var cache: TtlCache<LenientDisputeMetric>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeMetricStore {
    override fun find(code: String): LenientDisputeMetric? {
        val hit = (cache.get(code) as LenientDisputeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeMetric> = inner.list()
}

fun newCachedLenientDisputeMetricStore(inner: LenientDisputeMetricStore, ttl: Int, clock: Clock): LenientDisputeMetricStore {
    return CachedLenientDisputeMetricStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute metrics. Empty fields match all. */
data class LenientDisputeMetricQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<LenientDisputeMetric>): MutableList<LenientDisputeMetric> {
        val matched = filter(items) { candidate: LenientDisputeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeMetrics(store: LenientDisputeMetricStore, query: LenientDisputeMetricQuery): MutableList<LenientDisputeMetric> {
    return query.apply(store.list())
}
