package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient broker estimates by code. */
interface LenientBrokerEstimateStore {
    fun find(code: String): LenientBrokerEstimate?
    fun save(item: LenientBrokerEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBrokerEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBrokerEstimateStore(
    var items: MutableMap<String, LenientBrokerEstimate>,
    var order: MutableList<String>,
) : LenientBrokerEstimateStore {
    override fun find(code: String): LenientBrokerEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBrokerEstimate) {
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

    override fun list(): MutableList<LenientBrokerEstimate> {
        val out = mutableListOf<LenientBrokerEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBrokerEstimateStore(): LenientBrokerEstimateStore {
    return MemoryLenientBrokerEstimateStore(
        mutableMapOf<String, LenientBrokerEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBrokerEstimateStore(
    var inner: LenientBrokerEstimateStore,
    var cache: TtlCache<LenientBrokerEstimate>,
    var hits: Int,
    var misses: Int,
) : LenientBrokerEstimateStore {
    override fun find(code: String): LenientBrokerEstimate? {
        val hit = (cache.get(code) as LenientBrokerEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBrokerEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBrokerEstimate> = inner.list()
}

fun newCachedLenientBrokerEstimateStore(inner: LenientBrokerEstimateStore, ttl: Int, clock: Clock): LenientBrokerEstimateStore {
    return CachedLenientBrokerEstimateStore(
        inner = inner,
        cache = newTtlCache<LenientBrokerEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient broker estimates. Empty fields match all. */
data class LenientBrokerEstimateQuery(
    var source: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBrokerEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientBrokerEstimate>): MutableList<LenientBrokerEstimate> {
        val matched = filter(items) { candidate: LenientBrokerEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBrokerEstimates(store: LenientBrokerEstimateStore, query: LenientBrokerEstimateQuery): MutableList<LenientBrokerEstimate> {
    return query.apply(store.list())
}
