package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery summaries by code. */
interface RedeliverySummaryStore {
    fun find(code: String): RedeliverySummary?
    fun save(item: RedeliverySummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliverySummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliverySummaryStore(
    var items: MutableMap<String, RedeliverySummary>,
    var order: MutableList<String>,
) : RedeliverySummaryStore {
    override fun find(code: String): RedeliverySummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliverySummary) {
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

    override fun list(): MutableList<RedeliverySummary> {
        val out = mutableListOf<RedeliverySummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliverySummaryStore(): RedeliverySummaryStore {
    return MemoryRedeliverySummaryStore(mutableMapOf<String, RedeliverySummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliverySummaryStore(
    var inner: RedeliverySummaryStore,
    var cache: TtlCache<RedeliverySummary>,
    var hits: Int,
    var misses: Int,
) : RedeliverySummaryStore {
    override fun find(code: String): RedeliverySummary? {
        val hit = (cache.get(code) as RedeliverySummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliverySummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliverySummary> = inner.list()
}

fun newCachedRedeliverySummaryStore(inner: RedeliverySummaryStore, ttl: Int, clock: Clock): RedeliverySummaryStore {
    return CachedRedeliverySummaryStore(
        inner = inner,
        cache = newTtlCache<RedeliverySummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery summaries. Empty fields match all. */
data class RedeliverySummaryQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliverySummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliverySummary>): MutableList<RedeliverySummary> {
        val matched = filter(items) { candidate: RedeliverySummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliverySummaries(store: RedeliverySummaryStore, query: RedeliverySummaryQuery): MutableList<RedeliverySummary> {
    return query.apply(store.list())
}
