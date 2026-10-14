package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled transit time summaries by code. */
interface ScheduledTransitTimeSummaryStore {
    fun find(code: String): ScheduledTransitTimeSummary?
    fun save(item: ScheduledTransitTimeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTransitTimeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTransitTimeSummaryStore(
    var items: MutableMap<String, ScheduledTransitTimeSummary>,
    var order: MutableList<String>,
) : ScheduledTransitTimeSummaryStore {
    override fun find(code: String): ScheduledTransitTimeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTransitTimeSummary) {
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

    override fun list(): MutableList<ScheduledTransitTimeSummary> {
        val out = mutableListOf<ScheduledTransitTimeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTransitTimeSummaryStore(): ScheduledTransitTimeSummaryStore {
    return MemoryScheduledTransitTimeSummaryStore(
        mutableMapOf<String, ScheduledTransitTimeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTransitTimeSummaryStore(
    var inner: ScheduledTransitTimeSummaryStore,
    var cache: TtlCache<ScheduledTransitTimeSummary>,
    var hits: Int,
    var misses: Int,
) : ScheduledTransitTimeSummaryStore {
    override fun find(code: String): ScheduledTransitTimeSummary? {
        val hit = (cache.get(code) as ScheduledTransitTimeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTransitTimeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTransitTimeSummary> = inner.list()
}

fun newCachedScheduledTransitTimeSummaryStore(inner: ScheduledTransitTimeSummaryStore, ttl: Int, clock: Clock): ScheduledTransitTimeSummaryStore {
    return CachedScheduledTransitTimeSummaryStore(
        inner = inner,
        cache = newTtlCache<ScheduledTransitTimeSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled transit time summaries. Empty fields match all. */
data class ScheduledTransitTimeSummaryQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTransitTimeSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTransitTimeSummary>): MutableList<ScheduledTransitTimeSummary> {
        val matched = filter(items) { candidate: ScheduledTransitTimeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTransitTimeSummaries(store: ScheduledTransitTimeSummaryStore, query: ScheduledTransitTimeSummaryQuery): MutableList<ScheduledTransitTimeSummary> {
    return query.apply(store.list())
}
