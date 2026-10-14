package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance summaries by code. */
interface ClearanceSummaryStore {
    fun find(code: String): ClearanceSummary?
    fun save(item: ClearanceSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceSummaryStore(
    var items: MutableMap<String, ClearanceSummary>,
    var order: MutableList<String>,
) : ClearanceSummaryStore {
    override fun find(code: String): ClearanceSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceSummary) {
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

    override fun list(): MutableList<ClearanceSummary> {
        val out = mutableListOf<ClearanceSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceSummaryStore(): ClearanceSummaryStore {
    return MemoryClearanceSummaryStore(mutableMapOf<String, ClearanceSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceSummaryStore(
    var inner: ClearanceSummaryStore,
    var cache: TtlCache<ClearanceSummary>,
    var hits: Int,
    var misses: Int,
) : ClearanceSummaryStore {
    override fun find(code: String): ClearanceSummary? {
        val hit = (cache.get(code) as ClearanceSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceSummary> = inner.list()
}

fun newCachedClearanceSummaryStore(inner: ClearanceSummaryStore, ttl: Int, clock: Clock): ClearanceSummaryStore {
    return CachedClearanceSummaryStore(
        inner = inner,
        cache = newTtlCache<ClearanceSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance summaries. Empty fields match all. */
data class ClearanceSummaryQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceSummary>): MutableList<ClearanceSummary> {
        val matched = filter(items) { candidate: ClearanceSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceSummaries(store: ClearanceSummaryStore, query: ClearanceSummaryQuery): MutableList<ClearanceSummary> {
    return query.apply(store.list())
}
