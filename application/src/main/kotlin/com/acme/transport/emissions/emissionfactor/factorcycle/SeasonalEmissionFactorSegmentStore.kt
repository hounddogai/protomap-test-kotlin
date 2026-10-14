package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal emission factor segments by code. */
interface SeasonalEmissionFactorSegmentStore {
    fun find(code: String): SeasonalEmissionFactorSegment?
    fun save(item: SeasonalEmissionFactorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEmissionFactorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEmissionFactorSegmentStore(
    var items: MutableMap<String, SeasonalEmissionFactorSegment>,
    var order: MutableList<String>,
) : SeasonalEmissionFactorSegmentStore {
    override fun find(code: String): SeasonalEmissionFactorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEmissionFactorSegment) {
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

    override fun list(): MutableList<SeasonalEmissionFactorSegment> {
        val out = mutableListOf<SeasonalEmissionFactorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEmissionFactorSegmentStore(): SeasonalEmissionFactorSegmentStore {
    return MemorySeasonalEmissionFactorSegmentStore(
        mutableMapOf<String, SeasonalEmissionFactorSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEmissionFactorSegmentStore(
    var inner: SeasonalEmissionFactorSegmentStore,
    var cache: TtlCache<SeasonalEmissionFactorSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalEmissionFactorSegmentStore {
    override fun find(code: String): SeasonalEmissionFactorSegment? {
        val hit = (cache.get(code) as SeasonalEmissionFactorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEmissionFactorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEmissionFactorSegment> = inner.list()
}

fun newCachedSeasonalEmissionFactorSegmentStore(inner: SeasonalEmissionFactorSegmentStore, ttl: Int, clock: Clock): SeasonalEmissionFactorSegmentStore {
    return CachedSeasonalEmissionFactorSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalEmissionFactorSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal emission factor segments. Empty fields match all. */
data class SeasonalEmissionFactorSegmentQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEmissionFactorSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEmissionFactorSegment>): MutableList<SeasonalEmissionFactorSegment> {
        val matched = filter(items) { candidate: SeasonalEmissionFactorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEmissionFactorSegments(store: SeasonalEmissionFactorSegmentStore, query: SeasonalEmissionFactorSegmentQuery): MutableList<SeasonalEmissionFactorSegment> {
    return query.apply(store.list())
}
