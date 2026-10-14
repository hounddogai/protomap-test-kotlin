package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard summaries by code. */
interface ScorecardSummaryStore {
    fun find(code: String): ScorecardSummary?
    fun save(item: ScorecardSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardSummaryStore(
    var items: MutableMap<String, ScorecardSummary>,
    var order: MutableList<String>,
) : ScorecardSummaryStore {
    override fun find(code: String): ScorecardSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardSummary) {
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

    override fun list(): MutableList<ScorecardSummary> {
        val out = mutableListOf<ScorecardSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardSummaryStore(): ScorecardSummaryStore {
    return MemoryScorecardSummaryStore(mutableMapOf<String, ScorecardSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardSummaryStore(
    var inner: ScorecardSummaryStore,
    var cache: TtlCache<ScorecardSummary>,
    var hits: Int,
    var misses: Int,
) : ScorecardSummaryStore {
    override fun find(code: String): ScorecardSummary? {
        val hit = (cache.get(code) as ScorecardSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardSummary> = inner.list()
}

fun newCachedScorecardSummaryStore(inner: ScorecardSummaryStore, ttl: Int, clock: Clock): ScorecardSummaryStore {
    return CachedScorecardSummaryStore(
        inner = inner,
        cache = newTtlCache<ScorecardSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard summaries. Empty fields match all. */
data class ScorecardSummaryQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardSummary>): MutableList<ScorecardSummary> {
        val matched = filter(items) { candidate: ScorecardSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardSummaries(store: ScorecardSummaryStore, query: ScorecardSummaryQuery): MutableList<ScorecardSummary> {
    return query.apply(store.list())
}
