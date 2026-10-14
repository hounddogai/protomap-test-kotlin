package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal trailer adjustments by code. */
interface SeasonalTrailerAdjustmentStore {
    fun find(code: String): SeasonalTrailerAdjustment?
    fun save(item: SeasonalTrailerAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerAdjustmentStore(
    var items: MutableMap<String, SeasonalTrailerAdjustment>,
    var order: MutableList<String>,
) : SeasonalTrailerAdjustmentStore {
    override fun find(code: String): SeasonalTrailerAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerAdjustment) {
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

    override fun list(): MutableList<SeasonalTrailerAdjustment> {
        val out = mutableListOf<SeasonalTrailerAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerAdjustmentStore(): SeasonalTrailerAdjustmentStore {
    return MemorySeasonalTrailerAdjustmentStore(
        mutableMapOf<String, SeasonalTrailerAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerAdjustmentStore(
    var inner: SeasonalTrailerAdjustmentStore,
    var cache: TtlCache<SeasonalTrailerAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerAdjustmentStore {
    override fun find(code: String): SeasonalTrailerAdjustment? {
        val hit = (cache.get(code) as SeasonalTrailerAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerAdjustment> = inner.list()
}

fun newCachedSeasonalTrailerAdjustmentStore(inner: SeasonalTrailerAdjustmentStore, ttl: Int, clock: Clock): SeasonalTrailerAdjustmentStore {
    return CachedSeasonalTrailerAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer adjustments. Empty fields match all. */
data class SeasonalTrailerAdjustmentQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerAdjustment>): MutableList<SeasonalTrailerAdjustment> {
        val matched = filter(items) { candidate: SeasonalTrailerAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerAdjustments(store: SeasonalTrailerAdjustmentStore, query: SeasonalTrailerAdjustmentQuery): MutableList<SeasonalTrailerAdjustment> {
    return query.apply(store.list())
}
