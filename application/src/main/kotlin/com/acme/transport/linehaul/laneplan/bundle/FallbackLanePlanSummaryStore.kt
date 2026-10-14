package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan summaries by code. */
interface FallbackLanePlanSummaryStore {
    fun find(code: String): FallbackLanePlanSummary?
    fun save(item: FallbackLanePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanSummaryStore(
    var items: MutableMap<String, FallbackLanePlanSummary>,
    var order: MutableList<String>,
) : FallbackLanePlanSummaryStore {
    override fun find(code: String): FallbackLanePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanSummary) {
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

    override fun list(): MutableList<FallbackLanePlanSummary> {
        val out = mutableListOf<FallbackLanePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanSummaryStore(): FallbackLanePlanSummaryStore {
    return MemoryFallbackLanePlanSummaryStore(
        mutableMapOf<String, FallbackLanePlanSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanSummaryStore(
    var inner: FallbackLanePlanSummaryStore,
    var cache: TtlCache<FallbackLanePlanSummary>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanSummaryStore {
    override fun find(code: String): FallbackLanePlanSummary? {
        val hit = (cache.get(code) as FallbackLanePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanSummary> = inner.list()
}

fun newCachedFallbackLanePlanSummaryStore(inner: FallbackLanePlanSummaryStore, ttl: Int, clock: Clock): FallbackLanePlanSummaryStore {
    return CachedFallbackLanePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan summaries. Empty fields match all. */
data class FallbackLanePlanSummaryQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanSummary): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanSummary>): MutableList<FallbackLanePlanSummary> {
        val matched = filter(items) { candidate: FallbackLanePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanSummaries(store: FallbackLanePlanSummaryStore, query: FallbackLanePlanSummaryQuery): MutableList<FallbackLanePlanSummary> {
    return query.apply(store.list())
}
