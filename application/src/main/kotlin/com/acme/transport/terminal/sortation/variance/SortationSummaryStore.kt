package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation summaries by code. */
interface SortationSummaryStore {
    fun find(code: String): SortationSummary?
    fun save(item: SortationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationSummaryStore(
    var items: MutableMap<String, SortationSummary>,
    var order: MutableList<String>,
) : SortationSummaryStore {
    override fun find(code: String): SortationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationSummary) {
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

    override fun list(): MutableList<SortationSummary> {
        val out = mutableListOf<SortationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationSummaryStore(): SortationSummaryStore {
    return MemorySortationSummaryStore(mutableMapOf<String, SortationSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationSummaryStore(
    var inner: SortationSummaryStore,
    var cache: TtlCache<SortationSummary>,
    var hits: Int,
    var misses: Int,
) : SortationSummaryStore {
    override fun find(code: String): SortationSummary? {
        val hit = (cache.get(code) as SortationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationSummary> = inner.list()
}

fun newCachedSortationSummaryStore(inner: SortationSummaryStore, ttl: Int, clock: Clock): SortationSummaryStore {
    return CachedSortationSummaryStore(
        inner = inner,
        cache = newTtlCache<SortationSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation summaries. Empty fields match all. */
data class SortationSummaryQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<SortationSummary>): MutableList<SortationSummary> {
        val matched = filter(items) { candidate: SortationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationSummaries(store: SortationSummaryStore, query: SortationSummaryQuery): MutableList<SortationSummary> {
    return query.apply(store.list())
}
