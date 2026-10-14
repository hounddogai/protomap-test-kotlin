package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal fuel surcharge slots by code. */
interface SeasonalFuelSurchargeSlotStore {
    fun find(code: String): SeasonalFuelSurchargeSlot?
    fun save(item: SeasonalFuelSurchargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFuelSurchargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFuelSurchargeSlotStore(
    var items: MutableMap<String, SeasonalFuelSurchargeSlot>,
    var order: MutableList<String>,
) : SeasonalFuelSurchargeSlotStore {
    override fun find(code: String): SeasonalFuelSurchargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFuelSurchargeSlot) {
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

    override fun list(): MutableList<SeasonalFuelSurchargeSlot> {
        val out = mutableListOf<SeasonalFuelSurchargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFuelSurchargeSlotStore(): SeasonalFuelSurchargeSlotStore {
    return MemorySeasonalFuelSurchargeSlotStore(
        mutableMapOf<String, SeasonalFuelSurchargeSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFuelSurchargeSlotStore(
    var inner: SeasonalFuelSurchargeSlotStore,
    var cache: TtlCache<SeasonalFuelSurchargeSlot>,
    var hits: Int,
    var misses: Int,
) : SeasonalFuelSurchargeSlotStore {
    override fun find(code: String): SeasonalFuelSurchargeSlot? {
        val hit = (cache.get(code) as SeasonalFuelSurchargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFuelSurchargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFuelSurchargeSlot> = inner.list()
}

fun newCachedSeasonalFuelSurchargeSlotStore(inner: SeasonalFuelSurchargeSlotStore, ttl: Int, clock: Clock): SeasonalFuelSurchargeSlotStore {
    return CachedSeasonalFuelSurchargeSlotStore(
        inner = inner,
        cache = newTtlCache<SeasonalFuelSurchargeSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal fuel surcharge slots. Empty fields match all. */
data class SeasonalFuelSurchargeSlotQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFuelSurchargeSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFuelSurchargeSlot>): MutableList<SeasonalFuelSurchargeSlot> {
        val matched = filter(items) { candidate: SeasonalFuelSurchargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFuelSurchargeSlots(store: SeasonalFuelSurchargeSlotStore, query: SeasonalFuelSurchargeSlotQuery): MutableList<SeasonalFuelSurchargeSlot> {
    return query.apply(store.list())
}
