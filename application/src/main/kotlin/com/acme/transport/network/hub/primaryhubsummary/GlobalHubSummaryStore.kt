package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global hub summaries by code. */
interface GlobalHubSummaryStore {
    fun find(code: String): GlobalHubSummary?
    fun save(item: GlobalHubSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalHubSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalHubSummaryStore(
    var items: MutableMap<String, GlobalHubSummary>,
    var order: MutableList<String>,
) : GlobalHubSummaryStore {
    override fun find(code: String): GlobalHubSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalHubSummary) {
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

    override fun list(): MutableList<GlobalHubSummary> {
        val out = mutableListOf<GlobalHubSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalHubSummaryStore(): GlobalHubSummaryStore {
    return MemoryGlobalHubSummaryStore(mutableMapOf<String, GlobalHubSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalHubSummaryStore(
    var inner: GlobalHubSummaryStore,
    var cache: TtlCache<GlobalHubSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalHubSummaryStore {
    override fun find(code: String): GlobalHubSummary? {
        val hit = (cache.get(code) as GlobalHubSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalHubSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalHubSummary> = inner.list()
}

fun newCachedGlobalHubSummaryStore(inner: GlobalHubSummaryStore, ttl: Int, clock: Clock): GlobalHubSummaryStore {
    return CachedGlobalHubSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalHubSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global hub summaries. Empty fields match all. */
data class GlobalHubSummaryQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalHubSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalHubSummary>): MutableList<GlobalHubSummary> {
        val matched = filter(items) { candidate: GlobalHubSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalHubSummaries(store: GlobalHubSummaryStore, query: GlobalHubSummaryQuery): MutableList<GlobalHubSummary> {
    return query.apply(store.list())
}
