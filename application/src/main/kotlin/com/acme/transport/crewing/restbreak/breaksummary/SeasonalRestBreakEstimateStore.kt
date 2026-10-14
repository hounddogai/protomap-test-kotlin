package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal rest break estimates by code. */
interface SeasonalRestBreakEstimateStore {
    fun find(code: String): SeasonalRestBreakEstimate?
    fun save(item: SeasonalRestBreakEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRestBreakEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRestBreakEstimateStore(
    var items: MutableMap<String, SeasonalRestBreakEstimate>,
    var order: MutableList<String>,
) : SeasonalRestBreakEstimateStore {
    override fun find(code: String): SeasonalRestBreakEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRestBreakEstimate) {
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

    override fun list(): MutableList<SeasonalRestBreakEstimate> {
        val out = mutableListOf<SeasonalRestBreakEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRestBreakEstimateStore(): SeasonalRestBreakEstimateStore {
    return MemorySeasonalRestBreakEstimateStore(
        mutableMapOf<String, SeasonalRestBreakEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRestBreakEstimateStore(
    var inner: SeasonalRestBreakEstimateStore,
    var cache: TtlCache<SeasonalRestBreakEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalRestBreakEstimateStore {
    override fun find(code: String): SeasonalRestBreakEstimate? {
        val hit = (cache.get(code) as SeasonalRestBreakEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRestBreakEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRestBreakEstimate> = inner.list()
}

fun newCachedSeasonalRestBreakEstimateStore(inner: SeasonalRestBreakEstimateStore, ttl: Int, clock: Clock): SeasonalRestBreakEstimateStore {
    return CachedSeasonalRestBreakEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalRestBreakEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal rest break estimates. Empty fields match all. */
data class SeasonalRestBreakEstimateQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRestBreakEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRestBreakEstimate>): MutableList<SeasonalRestBreakEstimate> {
        val matched = filter(items) { candidate: SeasonalRestBreakEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRestBreakEstimates(store: SeasonalRestBreakEstimateStore, query: SeasonalRestBreakEstimateQuery): MutableList<SeasonalRestBreakEstimate> {
    return query.apply(store.list())
}
