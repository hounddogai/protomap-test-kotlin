package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled parcel summaries by code. */
interface ScheduledParcelSummaryStore {
    fun find(code: String): ScheduledParcelSummary?
    fun save(item: ScheduledParcelSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledParcelSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledParcelSummaryStore(
    var items: MutableMap<String, ScheduledParcelSummary>,
    var order: MutableList<String>,
) : ScheduledParcelSummaryStore {
    override fun find(code: String): ScheduledParcelSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledParcelSummary) {
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

    override fun list(): MutableList<ScheduledParcelSummary> {
        val out = mutableListOf<ScheduledParcelSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledParcelSummaryStore(): ScheduledParcelSummaryStore {
    return MemoryScheduledParcelSummaryStore(
        mutableMapOf<String, ScheduledParcelSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledParcelSummaryStore(
    var inner: ScheduledParcelSummaryStore,
    var cache: TtlCache<ScheduledParcelSummary>,
    var hits: Int,
    var misses: Int,
) : ScheduledParcelSummaryStore {
    override fun find(code: String): ScheduledParcelSummary? {
        val hit = (cache.get(code) as ScheduledParcelSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledParcelSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledParcelSummary> = inner.list()
}

fun newCachedScheduledParcelSummaryStore(inner: ScheduledParcelSummaryStore, ttl: Int, clock: Clock): ScheduledParcelSummaryStore {
    return CachedScheduledParcelSummaryStore(
        inner = inner,
        cache = newTtlCache<ScheduledParcelSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled parcel summaries. Empty fields match all. */
data class ScheduledParcelSummaryQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledParcelSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledParcelSummary>): MutableList<ScheduledParcelSummary> {
        val matched = filter(items) { candidate: ScheduledParcelSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledParcelSummaries(store: ScheduledParcelSummaryStore, query: ScheduledParcelSummaryQuery): MutableList<ScheduledParcelSummary> {
    return query.apply(store.list())
}
