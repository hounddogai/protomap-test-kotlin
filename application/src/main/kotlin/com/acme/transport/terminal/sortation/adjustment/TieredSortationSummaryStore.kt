package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation summaries by code. */
interface TieredSortationSummaryStore {
    fun find(code: String): TieredSortationSummary?
    fun save(item: TieredSortationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationSummaryStore(
    var items: MutableMap<String, TieredSortationSummary>,
    var order: MutableList<String>,
) : TieredSortationSummaryStore {
    override fun find(code: String): TieredSortationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationSummary) {
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

    override fun list(): MutableList<TieredSortationSummary> {
        val out = mutableListOf<TieredSortationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationSummaryStore(): TieredSortationSummaryStore {
    return MemoryTieredSortationSummaryStore(
        mutableMapOf<String, TieredSortationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationSummaryStore(
    var inner: TieredSortationSummaryStore,
    var cache: TtlCache<TieredSortationSummary>,
    var hits: Int,
    var misses: Int,
) : TieredSortationSummaryStore {
    override fun find(code: String): TieredSortationSummary? {
        val hit = (cache.get(code) as TieredSortationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationSummary> = inner.list()
}

fun newCachedTieredSortationSummaryStore(inner: TieredSortationSummaryStore, ttl: Int, clock: Clock): TieredSortationSummaryStore {
    return CachedTieredSortationSummaryStore(
        inner = inner,
        cache = newTtlCache<TieredSortationSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation summaries. Empty fields match all. */
data class TieredSortationSummaryQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSortationSummary>): MutableList<TieredSortationSummary> {
        val matched = filter(items) { candidate: TieredSortationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationSummaries(store: TieredSortationSummaryStore, query: TieredSortationSummaryQuery): MutableList<TieredSortationSummary> {
    return query.apply(store.list())
}
