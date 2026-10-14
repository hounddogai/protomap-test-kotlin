package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay estimates by code. */
interface RelayEstimateStore {
    fun find(code: String): RelayEstimate?
    fun save(item: RelayEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayEstimateStore(
    var items: MutableMap<String, RelayEstimate>,
    var order: MutableList<String>,
) : RelayEstimateStore {
    override fun find(code: String): RelayEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayEstimate) {
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

    override fun list(): MutableList<RelayEstimate> {
        val out = mutableListOf<RelayEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayEstimateStore(): RelayEstimateStore {
    return MemoryRelayEstimateStore(mutableMapOf<String, RelayEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayEstimateStore(
    var inner: RelayEstimateStore,
    var cache: TtlCache<RelayEstimate>,
    var hits: Int,
    var misses: Int,
) : RelayEstimateStore {
    override fun find(code: String): RelayEstimate? {
        val hit = (cache.get(code) as RelayEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayEstimate> = inner.list()
}

fun newCachedRelayEstimateStore(inner: RelayEstimateStore, ttl: Int, clock: Clock): RelayEstimateStore {
    return CachedRelayEstimateStore(
        inner = inner,
        cache = newTtlCache<RelayEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay estimates. Empty fields match all. */
data class RelayEstimateQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayEstimate>): MutableList<RelayEstimate> {
        val matched = filter(items) { candidate: RelayEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayEstimates(store: RelayEstimateStore, query: RelayEstimateQuery): MutableList<RelayEstimate> {
    return query.apply(store.list())
}
