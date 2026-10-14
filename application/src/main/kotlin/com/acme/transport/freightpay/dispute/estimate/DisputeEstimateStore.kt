package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute estimates by code. */
interface DisputeEstimateStore {
    fun find(code: String): DisputeEstimate?
    fun save(item: DisputeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeEstimateStore(
    var items: MutableMap<String, DisputeEstimate>,
    var order: MutableList<String>,
) : DisputeEstimateStore {
    override fun find(code: String): DisputeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeEstimate) {
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

    override fun list(): MutableList<DisputeEstimate> {
        val out = mutableListOf<DisputeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeEstimateStore(): DisputeEstimateStore {
    return MemoryDisputeEstimateStore(mutableMapOf<String, DisputeEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeEstimateStore(
    var inner: DisputeEstimateStore,
    var cache: TtlCache<DisputeEstimate>,
    var hits: Int,
    var misses: Int,
) : DisputeEstimateStore {
    override fun find(code: String): DisputeEstimate? {
        val hit = (cache.get(code) as DisputeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeEstimate> = inner.list()
}

fun newCachedDisputeEstimateStore(inner: DisputeEstimateStore, ttl: Int, clock: Clock): DisputeEstimateStore {
    return CachedDisputeEstimateStore(
        inner = inner,
        cache = newTtlCache<DisputeEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute estimates. Empty fields match all. */
data class DisputeEstimateQuery(
    var region: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<DisputeEstimate>): MutableList<DisputeEstimate> {
        val matched = filter(items) { candidate: DisputeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeEstimates(store: DisputeEstimateStore, query: DisputeEstimateQuery): MutableList<DisputeEstimate> {
    return query.apply(store.list())
}
