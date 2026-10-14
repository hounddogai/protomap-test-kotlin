package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal sortation adjustments by code. */
interface SeasonalSortationAdjustmentStore {
    fun find(code: String): SeasonalSortationAdjustment?
    fun save(item: SeasonalSortationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalSortationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalSortationAdjustmentStore(
    var items: MutableMap<String, SeasonalSortationAdjustment>,
    var order: MutableList<String>,
) : SeasonalSortationAdjustmentStore {
    override fun find(code: String): SeasonalSortationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalSortationAdjustment) {
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

    override fun list(): MutableList<SeasonalSortationAdjustment> {
        val out = mutableListOf<SeasonalSortationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalSortationAdjustmentStore(): SeasonalSortationAdjustmentStore {
    return MemorySeasonalSortationAdjustmentStore(
        mutableMapOf<String, SeasonalSortationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalSortationAdjustmentStore(
    var inner: SeasonalSortationAdjustmentStore,
    var cache: TtlCache<SeasonalSortationAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalSortationAdjustmentStore {
    override fun find(code: String): SeasonalSortationAdjustment? {
        val hit = (cache.get(code) as SeasonalSortationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalSortationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalSortationAdjustment> = inner.list()
}

fun newCachedSeasonalSortationAdjustmentStore(inner: SeasonalSortationAdjustmentStore, ttl: Int, clock: Clock): SeasonalSortationAdjustmentStore {
    return CachedSeasonalSortationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalSortationAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal sortation adjustments. Empty fields match all. */
data class SeasonalSortationAdjustmentQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalSortationAdjustment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalSortationAdjustment>): MutableList<SeasonalSortationAdjustment> {
        val matched = filter(items) { candidate: SeasonalSortationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalSortationAdjustments(store: SeasonalSortationAdjustmentStore, query: SeasonalSortationAdjustmentQuery): MutableList<SeasonalSortationAdjustment> {
    return query.apply(store.list())
}
