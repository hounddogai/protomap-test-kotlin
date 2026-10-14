package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker estimates by code. */
interface BrokerEstimateStore {
    fun find(code: String): BrokerEstimate?
    fun save(item: BrokerEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerEstimateStore(
    var items: MutableMap<String, BrokerEstimate>,
    var order: MutableList<String>,
) : BrokerEstimateStore {
    override fun find(code: String): BrokerEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerEstimate) {
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

    override fun list(): MutableList<BrokerEstimate> {
        val out = mutableListOf<BrokerEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerEstimateStore(): BrokerEstimateStore {
    return MemoryBrokerEstimateStore(mutableMapOf<String, BrokerEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerEstimateStore(
    var inner: BrokerEstimateStore,
    var cache: TtlCache<BrokerEstimate>,
    var hits: Int,
    var misses: Int,
) : BrokerEstimateStore {
    override fun find(code: String): BrokerEstimate? {
        val hit = (cache.get(code) as BrokerEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerEstimate> = inner.list()
}

fun newCachedBrokerEstimateStore(inner: BrokerEstimateStore, ttl: Int, clock: Clock): BrokerEstimateStore {
    return CachedBrokerEstimateStore(
        inner = inner,
        cache = newTtlCache<BrokerEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker estimates. Empty fields match all. */
data class BrokerEstimateQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerEstimate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerEstimate>): MutableList<BrokerEstimate> {
        val matched = filter(items) { candidate: BrokerEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerEstimates(store: BrokerEstimateStore, query: BrokerEstimateQuery): MutableList<BrokerEstimate> {
    return query.apply(store.list())
}
