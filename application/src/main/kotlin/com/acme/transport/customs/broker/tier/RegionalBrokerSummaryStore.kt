package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker summaries by code. */
interface RegionalBrokerSummaryStore {
    fun find(code: String): RegionalBrokerSummary?
    fun save(item: RegionalBrokerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerSummaryStore(
    var items: MutableMap<String, RegionalBrokerSummary>,
    var order: MutableList<String>,
) : RegionalBrokerSummaryStore {
    override fun find(code: String): RegionalBrokerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerSummary) {
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

    override fun list(): MutableList<RegionalBrokerSummary> {
        val out = mutableListOf<RegionalBrokerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerSummaryStore(): RegionalBrokerSummaryStore {
    return MemoryRegionalBrokerSummaryStore(
        mutableMapOf<String, RegionalBrokerSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerSummaryStore(
    var inner: RegionalBrokerSummaryStore,
    var cache: TtlCache<RegionalBrokerSummary>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerSummaryStore {
    override fun find(code: String): RegionalBrokerSummary? {
        val hit = (cache.get(code) as RegionalBrokerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerSummary> = inner.list()
}

fun newCachedRegionalBrokerSummaryStore(inner: RegionalBrokerSummaryStore, ttl: Int, clock: Clock): RegionalBrokerSummaryStore {
    return CachedRegionalBrokerSummaryStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker summaries. Empty fields match all. */
data class RegionalBrokerSummaryQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalBrokerSummary>): MutableList<RegionalBrokerSummary> {
        val matched = filter(items) { candidate: RegionalBrokerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerSummaries(store: RegionalBrokerSummaryStore, query: RegionalBrokerSummaryQuery): MutableList<RegionalBrokerSummary> {
    return query.apply(store.list())
}
