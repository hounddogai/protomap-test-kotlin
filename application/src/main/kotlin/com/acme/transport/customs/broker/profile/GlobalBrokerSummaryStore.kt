package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global broker summaries by code. */
interface GlobalBrokerSummaryStore {
    fun find(code: String): GlobalBrokerSummary?
    fun save(item: GlobalBrokerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalBrokerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalBrokerSummaryStore(
    var items: MutableMap<String, GlobalBrokerSummary>,
    var order: MutableList<String>,
) : GlobalBrokerSummaryStore {
    override fun find(code: String): GlobalBrokerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalBrokerSummary) {
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

    override fun list(): MutableList<GlobalBrokerSummary> {
        val out = mutableListOf<GlobalBrokerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalBrokerSummaryStore(): GlobalBrokerSummaryStore {
    return MemoryGlobalBrokerSummaryStore(mutableMapOf<String, GlobalBrokerSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalBrokerSummaryStore(
    var inner: GlobalBrokerSummaryStore,
    var cache: TtlCache<GlobalBrokerSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalBrokerSummaryStore {
    override fun find(code: String): GlobalBrokerSummary? {
        val hit = (cache.get(code) as GlobalBrokerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalBrokerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalBrokerSummary> = inner.list()
}

fun newCachedGlobalBrokerSummaryStore(inner: GlobalBrokerSummaryStore, ttl: Int, clock: Clock): GlobalBrokerSummaryStore {
    return CachedGlobalBrokerSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalBrokerSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global broker summaries. Empty fields match all. */
data class GlobalBrokerSummaryQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalBrokerSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalBrokerSummary>): MutableList<GlobalBrokerSummary> {
        val matched = filter(items) { candidate: GlobalBrokerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalBrokerSummaries(store: GlobalBrokerSummaryStore, query: GlobalBrokerSummaryQuery): MutableList<GlobalBrokerSummary> {
    return query.apply(store.list())
}
