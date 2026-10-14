package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive parcel slots by code. */
interface AdaptiveParcelSlotStore {
    fun find(code: String): AdaptiveParcelSlot?
    fun save(item: AdaptiveParcelSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveParcelSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveParcelSlotStore(
    var items: MutableMap<String, AdaptiveParcelSlot>,
    var order: MutableList<String>,
) : AdaptiveParcelSlotStore {
    override fun find(code: String): AdaptiveParcelSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveParcelSlot) {
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

    override fun list(): MutableList<AdaptiveParcelSlot> {
        val out = mutableListOf<AdaptiveParcelSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveParcelSlotStore(): AdaptiveParcelSlotStore {
    return MemoryAdaptiveParcelSlotStore(mutableMapOf<String, AdaptiveParcelSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveParcelSlotStore(
    var inner: AdaptiveParcelSlotStore,
    var cache: TtlCache<AdaptiveParcelSlot>,
    var hits: Int,
    var misses: Int,
) : AdaptiveParcelSlotStore {
    override fun find(code: String): AdaptiveParcelSlot? {
        val hit = (cache.get(code) as AdaptiveParcelSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveParcelSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveParcelSlot> = inner.list()
}

fun newCachedAdaptiveParcelSlotStore(inner: AdaptiveParcelSlotStore, ttl: Int, clock: Clock): AdaptiveParcelSlotStore {
    return CachedAdaptiveParcelSlotStore(
        inner = inner,
        cache = newTtlCache<AdaptiveParcelSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive parcel slots. Empty fields match all. */
data class AdaptiveParcelSlotQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveParcelSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveParcelSlot>): MutableList<AdaptiveParcelSlot> {
        val matched = filter(items) { candidate: AdaptiveParcelSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveParcelSlots(store: AdaptiveParcelSlotStore, query: AdaptiveParcelSlotQuery): MutableList<AdaptiveParcelSlot> {
    return query.apply(store.list())
}
