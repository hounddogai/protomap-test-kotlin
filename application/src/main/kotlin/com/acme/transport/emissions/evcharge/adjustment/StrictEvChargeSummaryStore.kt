package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge summaries by code. */
interface StrictEvChargeSummaryStore {
    fun find(code: String): StrictEvChargeSummary?
    fun save(item: StrictEvChargeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeSummaryStore(
    var items: MutableMap<String, StrictEvChargeSummary>,
    var order: MutableList<String>,
) : StrictEvChargeSummaryStore {
    override fun find(code: String): StrictEvChargeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeSummary) {
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

    override fun list(): MutableList<StrictEvChargeSummary> {
        val out = mutableListOf<StrictEvChargeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeSummaryStore(): StrictEvChargeSummaryStore {
    return MemoryStrictEvChargeSummaryStore(
        mutableMapOf<String, StrictEvChargeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeSummaryStore(
    var inner: StrictEvChargeSummaryStore,
    var cache: TtlCache<StrictEvChargeSummary>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeSummaryStore {
    override fun find(code: String): StrictEvChargeSummary? {
        val hit = (cache.get(code) as StrictEvChargeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeSummary> = inner.list()
}

fun newCachedStrictEvChargeSummaryStore(inner: StrictEvChargeSummaryStore, ttl: Int, clock: Clock): StrictEvChargeSummaryStore {
    return CachedStrictEvChargeSummaryStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge summaries. Empty fields match all. */
data class StrictEvChargeSummaryQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictEvChargeSummary>): MutableList<StrictEvChargeSummary> {
        val matched = filter(items) { candidate: StrictEvChargeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeSummaries(store: StrictEvChargeSummaryStore, query: StrictEvChargeSummaryQuery): MutableList<StrictEvChargeSummary> {
    return query.apply(store.list())
}
