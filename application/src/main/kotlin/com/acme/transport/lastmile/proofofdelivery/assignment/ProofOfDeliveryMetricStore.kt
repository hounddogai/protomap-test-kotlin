package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery metrics by code. */
interface ProofOfDeliveryMetricStore {
    fun find(code: String): ProofOfDeliveryMetric?
    fun save(item: ProofOfDeliveryMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryMetricStore(
    var items: MutableMap<String, ProofOfDeliveryMetric>,
    var order: MutableList<String>,
) : ProofOfDeliveryMetricStore {
    override fun find(code: String): ProofOfDeliveryMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryMetric) {
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

    override fun list(): MutableList<ProofOfDeliveryMetric> {
        val out = mutableListOf<ProofOfDeliveryMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryMetricStore(): ProofOfDeliveryMetricStore {
    return MemoryProofOfDeliveryMetricStore(
        mutableMapOf<String, ProofOfDeliveryMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryMetricStore(
    var inner: ProofOfDeliveryMetricStore,
    var cache: TtlCache<ProofOfDeliveryMetric>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryMetricStore {
    override fun find(code: String): ProofOfDeliveryMetric? {
        val hit = (cache.get(code) as ProofOfDeliveryMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryMetric> = inner.list()
}

fun newCachedProofOfDeliveryMetricStore(inner: ProofOfDeliveryMetricStore, ttl: Int, clock: Clock): ProofOfDeliveryMetricStore {
    return CachedProofOfDeliveryMetricStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery metrics. Empty fields match all. */
data class ProofOfDeliveryMetricQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryMetric>): MutableList<ProofOfDeliveryMetric> {
        val matched = filter(items) { candidate: ProofOfDeliveryMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryMetrics(store: ProofOfDeliveryMetricStore, query: ProofOfDeliveryMetricQuery): MutableList<ProofOfDeliveryMetric> {
    return query.apply(store.list())
}
