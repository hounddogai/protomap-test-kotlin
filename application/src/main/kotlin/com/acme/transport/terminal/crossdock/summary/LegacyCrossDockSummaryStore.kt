package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock summaries by code. */
interface LegacyCrossDockSummaryStore {
    fun find(code: String): LegacyCrossDockSummary?
    fun save(item: LegacyCrossDockSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockSummaryStore(
    var items: MutableMap<String, LegacyCrossDockSummary>,
    var order: MutableList<String>,
) : LegacyCrossDockSummaryStore {
    override fun find(code: String): LegacyCrossDockSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockSummary) {
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

    override fun list(): MutableList<LegacyCrossDockSummary> {
        val out = mutableListOf<LegacyCrossDockSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockSummaryStore(): LegacyCrossDockSummaryStore {
    return MemoryLegacyCrossDockSummaryStore(
        mutableMapOf<String, LegacyCrossDockSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockSummaryStore(
    var inner: LegacyCrossDockSummaryStore,
    var cache: TtlCache<LegacyCrossDockSummary>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockSummaryStore {
    override fun find(code: String): LegacyCrossDockSummary? {
        val hit = (cache.get(code) as LegacyCrossDockSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockSummary> = inner.list()
}

fun newCachedLegacyCrossDockSummaryStore(inner: LegacyCrossDockSummaryStore, ttl: Int, clock: Clock): LegacyCrossDockSummaryStore {
    return CachedLegacyCrossDockSummaryStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock summaries. Empty fields match all. */
data class LegacyCrossDockSummaryQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<LegacyCrossDockSummary>): MutableList<LegacyCrossDockSummary> {
        val matched = filter(items) { candidate: LegacyCrossDockSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockSummaries(store: LegacyCrossDockSummaryStore, query: LegacyCrossDockSummaryQuery): MutableList<LegacyCrossDockSummary> {
    return query.apply(store.list())
}
