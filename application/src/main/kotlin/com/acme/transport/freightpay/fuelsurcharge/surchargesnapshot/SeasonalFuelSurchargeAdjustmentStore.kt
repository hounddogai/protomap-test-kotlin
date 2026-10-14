package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal fuel surcharge adjustments by code. */
interface SeasonalFuelSurchargeAdjustmentStore {
    fun find(code: String): SeasonalFuelSurchargeAdjustment?
    fun save(item: SeasonalFuelSurchargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFuelSurchargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFuelSurchargeAdjustmentStore(
    var items: MutableMap<String, SeasonalFuelSurchargeAdjustment>,
    var order: MutableList<String>,
) : SeasonalFuelSurchargeAdjustmentStore {
    override fun find(code: String): SeasonalFuelSurchargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFuelSurchargeAdjustment) {
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

    override fun list(): MutableList<SeasonalFuelSurchargeAdjustment> {
        val out = mutableListOf<SeasonalFuelSurchargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFuelSurchargeAdjustmentStore(): SeasonalFuelSurchargeAdjustmentStore {
    return MemorySeasonalFuelSurchargeAdjustmentStore(
        mutableMapOf<String, SeasonalFuelSurchargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFuelSurchargeAdjustmentStore(
    var inner: SeasonalFuelSurchargeAdjustmentStore,
    var cache: TtlCache<SeasonalFuelSurchargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalFuelSurchargeAdjustmentStore {
    override fun find(code: String): SeasonalFuelSurchargeAdjustment? {
        val hit = (cache.get(code) as SeasonalFuelSurchargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFuelSurchargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFuelSurchargeAdjustment> = inner.list()
}

fun newCachedSeasonalFuelSurchargeAdjustmentStore(inner: SeasonalFuelSurchargeAdjustmentStore, ttl: Int, clock: Clock): SeasonalFuelSurchargeAdjustmentStore {
    return CachedSeasonalFuelSurchargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalFuelSurchargeAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal fuel surcharge adjustments. Empty fields match all. */
data class SeasonalFuelSurchargeAdjustmentQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFuelSurchargeAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFuelSurchargeAdjustment>): MutableList<SeasonalFuelSurchargeAdjustment> {
        val matched = filter(items) { candidate: SeasonalFuelSurchargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFuelSurchargeAdjustments(store: SeasonalFuelSurchargeAdjustmentStore, query: SeasonalFuelSurchargeAdjustmentQuery): MutableList<SeasonalFuelSurchargeAdjustment> {
    return query.apply(store.list())
}
