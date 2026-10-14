package com.acme.transport.network.zone.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback zone summaries by code. */
interface FallbackZoneSummaryStore {
    fun find(code: String): FallbackZoneSummary?
    fun save(item: FallbackZoneSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackZoneSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackZoneSummaryStore(
    var items: MutableMap<String, FallbackZoneSummary>,
    var order: MutableList<String>,
) : FallbackZoneSummaryStore {
    override fun find(code: String): FallbackZoneSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackZoneSummary) {
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

    override fun list(): MutableList<FallbackZoneSummary> {
        val out = mutableListOf<FallbackZoneSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackZoneSummaryStore(): FallbackZoneSummaryStore {
    return MemoryFallbackZoneSummaryStore(mutableMapOf<String, FallbackZoneSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackZoneSummaryStore(
    var inner: FallbackZoneSummaryStore,
    var cache: TtlCache<FallbackZoneSummary>,
    var hits: Int,
    var misses: Int,
) : FallbackZoneSummaryStore {
    override fun find(code: String): FallbackZoneSummary? {
        val hit = (cache.get(code) as FallbackZoneSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackZoneSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackZoneSummary> = inner.list()
}

fun newCachedFallbackZoneSummaryStore(inner: FallbackZoneSummaryStore, ttl: Int, clock: Clock): FallbackZoneSummaryStore {
    return CachedFallbackZoneSummaryStore(
        inner = inner,
        cache = newTtlCache<FallbackZoneSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback zone summaries. Empty fields match all. */
data class FallbackZoneSummaryQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackZoneSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackZoneSummary>): MutableList<FallbackZoneSummary> {
        val matched = filter(items) { candidate: FallbackZoneSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackZoneSummaries(store: FallbackZoneSummaryStore, query: FallbackZoneSummaryQuery): MutableList<FallbackZoneSummary> {
    return query.apply(store.list())
}
