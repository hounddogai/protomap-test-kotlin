package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock summaries by code. */
interface CrossDockSummaryStore {
    fun find(code: String): CrossDockSummary?
    fun save(item: CrossDockSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockSummaryStore(
    var items: MutableMap<String, CrossDockSummary>,
    var order: MutableList<String>,
) : CrossDockSummaryStore {
    override fun find(code: String): CrossDockSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockSummary) {
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

    override fun list(): MutableList<CrossDockSummary> {
        val out = mutableListOf<CrossDockSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockSummaryStore(): CrossDockSummaryStore {
    return MemoryCrossDockSummaryStore(mutableMapOf<String, CrossDockSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockSummaryStore(
    var inner: CrossDockSummaryStore,
    var cache: TtlCache<CrossDockSummary>,
    var hits: Int,
    var misses: Int,
) : CrossDockSummaryStore {
    override fun find(code: String): CrossDockSummary? {
        val hit = (cache.get(code) as CrossDockSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockSummary> = inner.list()
}

fun newCachedCrossDockSummaryStore(inner: CrossDockSummaryStore, ttl: Int, clock: Clock): CrossDockSummaryStore {
    return CachedCrossDockSummaryStore(
        inner = inner,
        cache = newTtlCache<CrossDockSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock summaries. Empty fields match all. */
data class CrossDockSummaryQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockSummary>): MutableList<CrossDockSummary> {
        val matched = filter(items) { candidate: CrossDockSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockSummaries(store: CrossDockSummaryStore, query: CrossDockSummaryQuery): MutableList<CrossDockSummary> {
    return query.apply(store.list())
}
