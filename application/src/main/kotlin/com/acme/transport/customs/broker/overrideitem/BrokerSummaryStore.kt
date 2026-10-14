package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker summaries by code. */
interface BrokerSummaryStore {
    fun find(code: String): BrokerSummary?
    fun save(item: BrokerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerSummaryStore(
    var items: MutableMap<String, BrokerSummary>,
    var order: MutableList<String>,
) : BrokerSummaryStore {
    override fun find(code: String): BrokerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerSummary) {
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

    override fun list(): MutableList<BrokerSummary> {
        val out = mutableListOf<BrokerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerSummaryStore(): BrokerSummaryStore {
    return MemoryBrokerSummaryStore(mutableMapOf<String, BrokerSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerSummaryStore(
    var inner: BrokerSummaryStore,
    var cache: TtlCache<BrokerSummary>,
    var hits: Int,
    var misses: Int,
) : BrokerSummaryStore {
    override fun find(code: String): BrokerSummary? {
        val hit = (cache.get(code) as BrokerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerSummary> = inner.list()
}

fun newCachedBrokerSummaryStore(inner: BrokerSummaryStore, ttl: Int, clock: Clock): BrokerSummaryStore {
    return CachedBrokerSummaryStore(
        inner = inner,
        cache = newTtlCache<BrokerSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker summaries. Empty fields match all. */
data class BrokerSummaryQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerSummary): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerSummary>): MutableList<BrokerSummary> {
        val matched = filter(items) { candidate: BrokerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerSummaries(store: BrokerSummaryStore, query: BrokerSummaryQuery): MutableList<BrokerSummary> {
    return query.apply(store.list())
}
