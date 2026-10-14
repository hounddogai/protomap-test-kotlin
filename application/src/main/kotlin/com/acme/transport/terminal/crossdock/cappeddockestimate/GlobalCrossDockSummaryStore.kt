package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock summaries by code. */
interface GlobalCrossDockSummaryStore {
    fun find(code: String): GlobalCrossDockSummary?
    fun save(item: GlobalCrossDockSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockSummaryStore(
    var items: MutableMap<String, GlobalCrossDockSummary>,
    var order: MutableList<String>,
) : GlobalCrossDockSummaryStore {
    override fun find(code: String): GlobalCrossDockSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockSummary) {
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

    override fun list(): MutableList<GlobalCrossDockSummary> {
        val out = mutableListOf<GlobalCrossDockSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockSummaryStore(): GlobalCrossDockSummaryStore {
    return MemoryGlobalCrossDockSummaryStore(
        mutableMapOf<String, GlobalCrossDockSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockSummaryStore(
    var inner: GlobalCrossDockSummaryStore,
    var cache: TtlCache<GlobalCrossDockSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockSummaryStore {
    override fun find(code: String): GlobalCrossDockSummary? {
        val hit = (cache.get(code) as GlobalCrossDockSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockSummary> = inner.list()
}

fun newCachedGlobalCrossDockSummaryStore(inner: GlobalCrossDockSummaryStore, ttl: Int, clock: Clock): GlobalCrossDockSummaryStore {
    return CachedGlobalCrossDockSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock summaries. Empty fields match all. */
data class GlobalCrossDockSummaryQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCrossDockSummary>): MutableList<GlobalCrossDockSummary> {
        val matched = filter(items) { candidate: GlobalCrossDockSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockSummaries(store: GlobalCrossDockSummaryStore, query: GlobalCrossDockSummaryQuery): MutableList<GlobalCrossDockSummary> {
    return query.apply(store.list())
}
