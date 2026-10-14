package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered dispute slots by code. */
interface TieredDisputeSlotStore {
    fun find(code: String): TieredDisputeSlot?
    fun save(item: TieredDisputeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDisputeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDisputeSlotStore(
    var items: MutableMap<String, TieredDisputeSlot>,
    var order: MutableList<String>,
) : TieredDisputeSlotStore {
    override fun find(code: String): TieredDisputeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDisputeSlot) {
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

    override fun list(): MutableList<TieredDisputeSlot> {
        val out = mutableListOf<TieredDisputeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDisputeSlotStore(): TieredDisputeSlotStore {
    return MemoryTieredDisputeSlotStore(mutableMapOf<String, TieredDisputeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDisputeSlotStore(
    var inner: TieredDisputeSlotStore,
    var cache: TtlCache<TieredDisputeSlot>,
    var hits: Int,
    var misses: Int,
) : TieredDisputeSlotStore {
    override fun find(code: String): TieredDisputeSlot? {
        val hit = (cache.get(code) as TieredDisputeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDisputeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDisputeSlot> = inner.list()
}

fun newCachedTieredDisputeSlotStore(inner: TieredDisputeSlotStore, ttl: Int, clock: Clock): TieredDisputeSlotStore {
    return CachedTieredDisputeSlotStore(
        inner = inner,
        cache = newTtlCache<TieredDisputeSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dispute slots. Empty fields match all. */
data class TieredDisputeSlotQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDisputeSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TieredDisputeSlot>): MutableList<TieredDisputeSlot> {
        val matched = filter(items) { candidate: TieredDisputeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDisputeSlots(store: TieredDisputeSlotStore, query: TieredDisputeSlotQuery): MutableList<TieredDisputeSlot> {
    return query.apply(store.list())
}
