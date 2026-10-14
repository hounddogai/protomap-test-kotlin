package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dock door adjustments by code. */
interface SeasonalDockDoorAdjustmentStore {
    fun find(code: String): SeasonalDockDoorAdjustment?
    fun save(item: SeasonalDockDoorAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDockDoorAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDockDoorAdjustmentStore(
    var items: MutableMap<String, SeasonalDockDoorAdjustment>,
    var order: MutableList<String>,
) : SeasonalDockDoorAdjustmentStore {
    override fun find(code: String): SeasonalDockDoorAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDockDoorAdjustment) {
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

    override fun list(): MutableList<SeasonalDockDoorAdjustment> {
        val out = mutableListOf<SeasonalDockDoorAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDockDoorAdjustmentStore(): SeasonalDockDoorAdjustmentStore {
    return MemorySeasonalDockDoorAdjustmentStore(
        mutableMapOf<String, SeasonalDockDoorAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDockDoorAdjustmentStore(
    var inner: SeasonalDockDoorAdjustmentStore,
    var cache: TtlCache<SeasonalDockDoorAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalDockDoorAdjustmentStore {
    override fun find(code: String): SeasonalDockDoorAdjustment? {
        val hit = (cache.get(code) as SeasonalDockDoorAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDockDoorAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDockDoorAdjustment> = inner.list()
}

fun newCachedSeasonalDockDoorAdjustmentStore(inner: SeasonalDockDoorAdjustmentStore, ttl: Int, clock: Clock): SeasonalDockDoorAdjustmentStore {
    return CachedSeasonalDockDoorAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalDockDoorAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dock door adjustments. Empty fields match all. */
data class SeasonalDockDoorAdjustmentQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDockDoorAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<SeasonalDockDoorAdjustment>): MutableList<SeasonalDockDoorAdjustment> {
        val matched = filter(items) { candidate: SeasonalDockDoorAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDockDoorAdjustments(store: SeasonalDockDoorAdjustmentStore, query: SeasonalDockDoorAdjustmentQuery): MutableList<SeasonalDockDoorAdjustment> {
    return query.apply(store.list())
}
