package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal coaching estimates by code. */
interface SeasonalCoachingEstimateStore {
    fun find(code: String): SeasonalCoachingEstimate?
    fun save(item: SeasonalCoachingEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCoachingEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCoachingEstimateStore(
    var items: MutableMap<String, SeasonalCoachingEstimate>,
    var order: MutableList<String>,
) : SeasonalCoachingEstimateStore {
    override fun find(code: String): SeasonalCoachingEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCoachingEstimate) {
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

    override fun list(): MutableList<SeasonalCoachingEstimate> {
        val out = mutableListOf<SeasonalCoachingEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCoachingEstimateStore(): SeasonalCoachingEstimateStore {
    return MemorySeasonalCoachingEstimateStore(
        mutableMapOf<String, SeasonalCoachingEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCoachingEstimateStore(
    var inner: SeasonalCoachingEstimateStore,
    var cache: TtlCache<SeasonalCoachingEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalCoachingEstimateStore {
    override fun find(code: String): SeasonalCoachingEstimate? {
        val hit = (cache.get(code) as SeasonalCoachingEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCoachingEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCoachingEstimate> = inner.list()
}

fun newCachedSeasonalCoachingEstimateStore(inner: SeasonalCoachingEstimateStore, ttl: Int, clock: Clock): SeasonalCoachingEstimateStore {
    return CachedSeasonalCoachingEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalCoachingEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal coaching estimates. Empty fields match all. */
data class SeasonalCoachingEstimateQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCoachingEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCoachingEstimate>): MutableList<SeasonalCoachingEstimate> {
        val matched = filter(items) { candidate: SeasonalCoachingEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCoachingEstimates(store: SeasonalCoachingEstimateStore, query: SeasonalCoachingEstimateQuery): MutableList<SeasonalCoachingEstimate> {
    return query.apply(store.list())
}
