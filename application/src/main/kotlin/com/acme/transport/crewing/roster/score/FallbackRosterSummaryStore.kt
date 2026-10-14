package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback roster summaries by code. */
interface FallbackRosterSummaryStore {
    fun find(code: String): FallbackRosterSummary?
    fun save(item: FallbackRosterSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRosterSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRosterSummaryStore(
    var items: MutableMap<String, FallbackRosterSummary>,
    var order: MutableList<String>,
) : FallbackRosterSummaryStore {
    override fun find(code: String): FallbackRosterSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRosterSummary) {
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

    override fun list(): MutableList<FallbackRosterSummary> {
        val out = mutableListOf<FallbackRosterSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRosterSummaryStore(): FallbackRosterSummaryStore {
    return MemoryFallbackRosterSummaryStore(
        mutableMapOf<String, FallbackRosterSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRosterSummaryStore(
    var inner: FallbackRosterSummaryStore,
    var cache: TtlCache<FallbackRosterSummary>,
    var hits: Int,
    var misses: Int,
) : FallbackRosterSummaryStore {
    override fun find(code: String): FallbackRosterSummary? {
        val hit = (cache.get(code) as FallbackRosterSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRosterSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRosterSummary> = inner.list()
}

fun newCachedFallbackRosterSummaryStore(inner: FallbackRosterSummaryStore, ttl: Int, clock: Clock): FallbackRosterSummaryStore {
    return CachedFallbackRosterSummaryStore(
        inner = inner,
        cache = newTtlCache<FallbackRosterSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback roster summaries. Empty fields match all. */
data class FallbackRosterSummaryQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRosterSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRosterSummary>): MutableList<FallbackRosterSummary> {
        val matched = filter(items) { candidate: FallbackRosterSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRosterSummaries(store: FallbackRosterSummaryStore, query: FallbackRosterSummaryQuery): MutableList<FallbackRosterSummary> {
    return query.apply(store.list())
}
