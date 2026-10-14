package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute metrics by code. */
interface DisputeMetricStore {
    fun find(code: String): DisputeMetric?
    fun save(item: DisputeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeMetricStore(
    var items: MutableMap<String, DisputeMetric>,
    var order: MutableList<String>,
) : DisputeMetricStore {
    override fun find(code: String): DisputeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeMetric) {
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

    override fun list(): MutableList<DisputeMetric> {
        val out = mutableListOf<DisputeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeMetricStore(): DisputeMetricStore {
    return MemoryDisputeMetricStore(mutableMapOf<String, DisputeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeMetricStore(
    var inner: DisputeMetricStore,
    var cache: TtlCache<DisputeMetric>,
    var hits: Int,
    var misses: Int,
) : DisputeMetricStore {
    override fun find(code: String): DisputeMetric? {
        val hit = (cache.get(code) as DisputeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeMetric> = inner.list()
}

fun newCachedDisputeMetricStore(inner: DisputeMetricStore, ttl: Int, clock: Clock): DisputeMetricStore {
    return CachedDisputeMetricStore(
        inner = inner,
        cache = newTtlCache<DisputeMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute metrics. Empty fields match all. */
data class DisputeMetricQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeMetric>): MutableList<DisputeMetric> {
        val matched = filter(items) { candidate: DisputeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeMetrics(store: DisputeMetricStore, query: DisputeMetricQuery): MutableList<DisputeMetric> {
    return query.apply(store.list())
}
