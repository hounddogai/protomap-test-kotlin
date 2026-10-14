package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff summaries by code. */
interface CutoffSummaryStore {
    fun find(code: String): CutoffSummary?
    fun save(item: CutoffSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffSummaryStore(
    var items: MutableMap<String, CutoffSummary>,
    var order: MutableList<String>,
) : CutoffSummaryStore {
    override fun find(code: String): CutoffSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffSummary) {
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

    override fun list(): MutableList<CutoffSummary> {
        val out = mutableListOf<CutoffSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffSummaryStore(): CutoffSummaryStore {
    return MemoryCutoffSummaryStore(mutableMapOf<String, CutoffSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffSummaryStore(
    var inner: CutoffSummaryStore,
    var cache: TtlCache<CutoffSummary>,
    var hits: Int,
    var misses: Int,
) : CutoffSummaryStore {
    override fun find(code: String): CutoffSummary? {
        val hit = (cache.get(code) as CutoffSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffSummary> = inner.list()
}

fun newCachedCutoffSummaryStore(inner: CutoffSummaryStore, ttl: Int, clock: Clock): CutoffSummaryStore {
    return CachedCutoffSummaryStore(
        inner = inner,
        cache = newTtlCache<CutoffSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff summaries. Empty fields match all. */
data class CutoffSummaryQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CutoffSummary>): MutableList<CutoffSummary> {
        val matched = filter(items) { candidate: CutoffSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffSummaries(store: CutoffSummaryStore, query: CutoffSummaryQuery): MutableList<CutoffSummary> {
    return query.apply(store.list())
}
