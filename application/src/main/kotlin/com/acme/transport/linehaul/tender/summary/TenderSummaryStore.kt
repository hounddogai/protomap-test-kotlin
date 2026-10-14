package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender summaries by code. */
interface TenderSummaryStore {
    fun find(code: String): TenderSummary?
    fun save(item: TenderSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderSummaryStore(
    var items: MutableMap<String, TenderSummary>,
    var order: MutableList<String>,
) : TenderSummaryStore {
    override fun find(code: String): TenderSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderSummary) {
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

    override fun list(): MutableList<TenderSummary> {
        val out = mutableListOf<TenderSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderSummaryStore(): TenderSummaryStore {
    return MemoryTenderSummaryStore(mutableMapOf<String, TenderSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderSummaryStore(
    var inner: TenderSummaryStore,
    var cache: TtlCache<TenderSummary>,
    var hits: Int,
    var misses: Int,
) : TenderSummaryStore {
    override fun find(code: String): TenderSummary? {
        val hit = (cache.get(code) as TenderSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderSummary> = inner.list()
}

fun newCachedTenderSummaryStore(inner: TenderSummaryStore, ttl: Int, clock: Clock): TenderSummaryStore {
    return CachedTenderSummaryStore(
        inner = inner,
        cache = newTtlCache<TenderSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender summaries. Empty fields match all. */
data class TenderSummaryQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderSummary): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderSummary>): MutableList<TenderSummary> {
        val matched = filter(items) { candidate: TenderSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderSummaries(store: TenderSummaryStore, query: TenderSummaryQuery): MutableList<TenderSummary> {
    return query.apply(store.list())
}
