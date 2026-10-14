package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive near miss summaries by code. */
interface AdaptiveNearMissSummaryStore {
    fun find(code: String): AdaptiveNearMissSummary?
    fun save(item: AdaptiveNearMissSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNearMissSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNearMissSummaryStore(
    var items: MutableMap<String, AdaptiveNearMissSummary>,
    var order: MutableList<String>,
) : AdaptiveNearMissSummaryStore {
    override fun find(code: String): AdaptiveNearMissSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNearMissSummary) {
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

    override fun list(): MutableList<AdaptiveNearMissSummary> {
        val out = mutableListOf<AdaptiveNearMissSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNearMissSummaryStore(): AdaptiveNearMissSummaryStore {
    return MemoryAdaptiveNearMissSummaryStore(
        mutableMapOf<String, AdaptiveNearMissSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNearMissSummaryStore(
    var inner: AdaptiveNearMissSummaryStore,
    var cache: TtlCache<AdaptiveNearMissSummary>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNearMissSummaryStore {
    override fun find(code: String): AdaptiveNearMissSummary? {
        val hit = (cache.get(code) as AdaptiveNearMissSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNearMissSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNearMissSummary> = inner.list()
}

fun newCachedAdaptiveNearMissSummaryStore(inner: AdaptiveNearMissSummaryStore, ttl: Int, clock: Clock): AdaptiveNearMissSummaryStore {
    return CachedAdaptiveNearMissSummaryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNearMissSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive near miss summaries. Empty fields match all. */
data class AdaptiveNearMissSummaryQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNearMissSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveNearMissSummary>): MutableList<AdaptiveNearMissSummary> {
        val matched = filter(items) { candidate: AdaptiveNearMissSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNearMissSummaries(store: AdaptiveNearMissSummaryStore, query: AdaptiveNearMissSummaryQuery): MutableList<AdaptiveNearMissSummary> {
    return query.apply(store.list())
}
