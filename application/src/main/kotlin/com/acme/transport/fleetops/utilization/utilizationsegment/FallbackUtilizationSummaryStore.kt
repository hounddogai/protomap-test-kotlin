package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback utilization summaries by code. */
interface FallbackUtilizationSummaryStore {
    fun find(code: String): FallbackUtilizationSummary?
    fun save(item: FallbackUtilizationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackUtilizationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackUtilizationSummaryStore(
    var items: MutableMap<String, FallbackUtilizationSummary>,
    var order: MutableList<String>,
) : FallbackUtilizationSummaryStore {
    override fun find(code: String): FallbackUtilizationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackUtilizationSummary) {
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

    override fun list(): MutableList<FallbackUtilizationSummary> {
        val out = mutableListOf<FallbackUtilizationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackUtilizationSummaryStore(): FallbackUtilizationSummaryStore {
    return MemoryFallbackUtilizationSummaryStore(
        mutableMapOf<String, FallbackUtilizationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackUtilizationSummaryStore(
    var inner: FallbackUtilizationSummaryStore,
    var cache: TtlCache<FallbackUtilizationSummary>,
    var hits: Int,
    var misses: Int,
) : FallbackUtilizationSummaryStore {
    override fun find(code: String): FallbackUtilizationSummary? {
        val hit = (cache.get(code) as FallbackUtilizationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackUtilizationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackUtilizationSummary> = inner.list()
}

fun newCachedFallbackUtilizationSummaryStore(inner: FallbackUtilizationSummaryStore, ttl: Int, clock: Clock): FallbackUtilizationSummaryStore {
    return CachedFallbackUtilizationSummaryStore(
        inner = inner,
        cache = newTtlCache<FallbackUtilizationSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback utilization summaries. Empty fields match all. */
data class FallbackUtilizationSummaryQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackUtilizationSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackUtilizationSummary>): MutableList<FallbackUtilizationSummary> {
        val matched = filter(items) { candidate: FallbackUtilizationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackUtilizationSummaries(store: FallbackUtilizationSummaryStore, query: FallbackUtilizationSummaryQuery): MutableList<FallbackUtilizationSummary> {
    return query.apply(store.list())
}
