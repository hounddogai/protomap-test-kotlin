package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled cross dock summaries by code. */
interface ScheduledCrossDockSummaryStore {
    fun find(code: String): ScheduledCrossDockSummary?
    fun save(item: ScheduledCrossDockSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCrossDockSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCrossDockSummaryStore(
    var items: MutableMap<String, ScheduledCrossDockSummary>,
    var order: MutableList<String>,
) : ScheduledCrossDockSummaryStore {
    override fun find(code: String): ScheduledCrossDockSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCrossDockSummary) {
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

    override fun list(): MutableList<ScheduledCrossDockSummary> {
        val out = mutableListOf<ScheduledCrossDockSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCrossDockSummaryStore(): ScheduledCrossDockSummaryStore {
    return MemoryScheduledCrossDockSummaryStore(
        mutableMapOf<String, ScheduledCrossDockSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCrossDockSummaryStore(
    var inner: ScheduledCrossDockSummaryStore,
    var cache: TtlCache<ScheduledCrossDockSummary>,
    var hits: Int,
    var misses: Int,
) : ScheduledCrossDockSummaryStore {
    override fun find(code: String): ScheduledCrossDockSummary? {
        val hit = (cache.get(code) as ScheduledCrossDockSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCrossDockSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCrossDockSummary> = inner.list()
}

fun newCachedScheduledCrossDockSummaryStore(inner: ScheduledCrossDockSummaryStore, ttl: Int, clock: Clock): ScheduledCrossDockSummaryStore {
    return CachedScheduledCrossDockSummaryStore(
        inner = inner,
        cache = newTtlCache<ScheduledCrossDockSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled cross dock summaries. Empty fields match all. */
data class ScheduledCrossDockSummaryQuery(
    var region: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCrossDockSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCrossDockSummary>): MutableList<ScheduledCrossDockSummary> {
        val matched = filter(items) { candidate: ScheduledCrossDockSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCrossDockSummaries(store: ScheduledCrossDockSummaryStore, query: ScheduledCrossDockSummaryQuery): MutableList<ScheduledCrossDockSummary> {
    return query.apply(store.list())
}
