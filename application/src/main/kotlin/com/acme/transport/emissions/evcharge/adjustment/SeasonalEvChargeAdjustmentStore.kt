package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal ev charge adjustments by code. */
interface SeasonalEvChargeAdjustmentStore {
    fun find(code: String): SeasonalEvChargeAdjustment?
    fun save(item: SeasonalEvChargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEvChargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEvChargeAdjustmentStore(
    var items: MutableMap<String, SeasonalEvChargeAdjustment>,
    var order: MutableList<String>,
) : SeasonalEvChargeAdjustmentStore {
    override fun find(code: String): SeasonalEvChargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEvChargeAdjustment) {
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

    override fun list(): MutableList<SeasonalEvChargeAdjustment> {
        val out = mutableListOf<SeasonalEvChargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEvChargeAdjustmentStore(): SeasonalEvChargeAdjustmentStore {
    return MemorySeasonalEvChargeAdjustmentStore(
        mutableMapOf<String, SeasonalEvChargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEvChargeAdjustmentStore(
    var inner: SeasonalEvChargeAdjustmentStore,
    var cache: TtlCache<SeasonalEvChargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalEvChargeAdjustmentStore {
    override fun find(code: String): SeasonalEvChargeAdjustment? {
        val hit = (cache.get(code) as SeasonalEvChargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEvChargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEvChargeAdjustment> = inner.list()
}

fun newCachedSeasonalEvChargeAdjustmentStore(inner: SeasonalEvChargeAdjustmentStore, ttl: Int, clock: Clock): SeasonalEvChargeAdjustmentStore {
    return CachedSeasonalEvChargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalEvChargeAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal ev charge adjustments. Empty fields match all. */
data class SeasonalEvChargeAdjustmentQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEvChargeAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEvChargeAdjustment>): MutableList<SeasonalEvChargeAdjustment> {
        val matched = filter(items) { candidate: SeasonalEvChargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEvChargeAdjustments(store: SeasonalEvChargeAdjustmentStore, query: SeasonalEvChargeAdjustmentQuery): MutableList<SeasonalEvChargeAdjustment> {
    return query.apply(store.list())
}
