package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal coaching variances by code. */
interface SeasonalCoachingVarianceStore {
    fun find(code: String): SeasonalCoachingVariance?
    fun save(item: SeasonalCoachingVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCoachingVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCoachingVarianceStore(
    var items: MutableMap<String, SeasonalCoachingVariance>,
    var order: MutableList<String>,
) : SeasonalCoachingVarianceStore {
    override fun find(code: String): SeasonalCoachingVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCoachingVariance) {
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

    override fun list(): MutableList<SeasonalCoachingVariance> {
        val out = mutableListOf<SeasonalCoachingVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCoachingVarianceStore(): SeasonalCoachingVarianceStore {
    return MemorySeasonalCoachingVarianceStore(
        mutableMapOf<String, SeasonalCoachingVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCoachingVarianceStore(
    var inner: SeasonalCoachingVarianceStore,
    var cache: TtlCache<SeasonalCoachingVariance>,
    var hits: Int,
    var misses: Int,
) : SeasonalCoachingVarianceStore {
    override fun find(code: String): SeasonalCoachingVariance? {
        val hit = (cache.get(code) as SeasonalCoachingVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCoachingVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCoachingVariance> = inner.list()
}

fun newCachedSeasonalCoachingVarianceStore(inner: SeasonalCoachingVarianceStore, ttl: Int, clock: Clock): SeasonalCoachingVarianceStore {
    return CachedSeasonalCoachingVarianceStore(
        inner = inner,
        cache = newTtlCache<SeasonalCoachingVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal coaching variances. Empty fields match all. */
data class SeasonalCoachingVarianceQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCoachingVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCoachingVariance>): MutableList<SeasonalCoachingVariance> {
        val matched = filter(items) { candidate: SeasonalCoachingVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCoachingVariances(store: SeasonalCoachingVarianceStore, query: SeasonalCoachingVarianceQuery): MutableList<SeasonalCoachingVariance> {
    return query.apply(store.list())
}
