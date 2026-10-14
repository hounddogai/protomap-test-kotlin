package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive accessorial slots by code. */
interface AdaptiveAccessorialSlotStore {
    fun find(code: String): AdaptiveAccessorialSlot?
    fun save(item: AdaptiveAccessorialSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAccessorialSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAccessorialSlotStore(
    var items: MutableMap<String, AdaptiveAccessorialSlot>,
    var order: MutableList<String>,
) : AdaptiveAccessorialSlotStore {
    override fun find(code: String): AdaptiveAccessorialSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAccessorialSlot) {
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

    override fun list(): MutableList<AdaptiveAccessorialSlot> {
        val out = mutableListOf<AdaptiveAccessorialSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAccessorialSlotStore(): AdaptiveAccessorialSlotStore {
    return MemoryAdaptiveAccessorialSlotStore(
        mutableMapOf<String, AdaptiveAccessorialSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAccessorialSlotStore(
    var inner: AdaptiveAccessorialSlotStore,
    var cache: TtlCache<AdaptiveAccessorialSlot>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAccessorialSlotStore {
    override fun find(code: String): AdaptiveAccessorialSlot? {
        val hit = (cache.get(code) as AdaptiveAccessorialSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAccessorialSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAccessorialSlot> = inner.list()
}

fun newCachedAdaptiveAccessorialSlotStore(inner: AdaptiveAccessorialSlotStore, ttl: Int, clock: Clock): AdaptiveAccessorialSlotStore {
    return CachedAdaptiveAccessorialSlotStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAccessorialSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive accessorial slots. Empty fields match all. */
data class AdaptiveAccessorialSlotQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAccessorialSlot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAccessorialSlot>): MutableList<AdaptiveAccessorialSlot> {
        val matched = filter(items) { candidate: AdaptiveAccessorialSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAccessorialSlots(store: AdaptiveAccessorialSlotStore, query: AdaptiveAccessorialSlotQuery): MutableList<AdaptiveAccessorialSlot> {
    return query.apply(store.list())
}
