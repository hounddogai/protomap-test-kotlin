package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal zone adjustments by code. */
interface SeasonalZoneAdjustmentStore {
    fun find(code: String): SeasonalZoneAdjustment?
    fun save(item: SeasonalZoneAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalZoneAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalZoneAdjustmentStore(
    var items: MutableMap<String, SeasonalZoneAdjustment>,
    var order: MutableList<String>,
) : SeasonalZoneAdjustmentStore {
    override fun find(code: String): SeasonalZoneAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalZoneAdjustment) {
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

    override fun list(): MutableList<SeasonalZoneAdjustment> {
        val out = mutableListOf<SeasonalZoneAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalZoneAdjustmentStore(): SeasonalZoneAdjustmentStore {
    return MemorySeasonalZoneAdjustmentStore(
        mutableMapOf<String, SeasonalZoneAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalZoneAdjustmentStore(
    var inner: SeasonalZoneAdjustmentStore,
    var cache: TtlCache<SeasonalZoneAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalZoneAdjustmentStore {
    override fun find(code: String): SeasonalZoneAdjustment? {
        val hit = (cache.get(code) as SeasonalZoneAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalZoneAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalZoneAdjustment> = inner.list()
}

fun newCachedSeasonalZoneAdjustmentStore(inner: SeasonalZoneAdjustmentStore, ttl: Int, clock: Clock): SeasonalZoneAdjustmentStore {
    return CachedSeasonalZoneAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalZoneAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal zone adjustments. Empty fields match all. */
data class SeasonalZoneAdjustmentQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalZoneAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalZoneAdjustment>): MutableList<SeasonalZoneAdjustment> {
        val matched = filter(items) { candidate: SeasonalZoneAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalZoneAdjustments(store: SeasonalZoneAdjustmentStore, query: SeasonalZoneAdjustmentQuery): MutableList<SeasonalZoneAdjustment> {
    return query.apply(store.list())
}
