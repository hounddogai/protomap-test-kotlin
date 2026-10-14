package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge slots by code. */
interface EvChargeSlotStore {
    fun find(code: String): EvChargeSlot?
    fun save(item: EvChargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeSlotStore(
    var items: MutableMap<String, EvChargeSlot>,
    var order: MutableList<String>,
) : EvChargeSlotStore {
    override fun find(code: String): EvChargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeSlot) {
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

    override fun list(): MutableList<EvChargeSlot> {
        val out = mutableListOf<EvChargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeSlotStore(): EvChargeSlotStore {
    return MemoryEvChargeSlotStore(mutableMapOf<String, EvChargeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeSlotStore(
    var inner: EvChargeSlotStore,
    var cache: TtlCache<EvChargeSlot>,
    var hits: Int,
    var misses: Int,
) : EvChargeSlotStore {
    override fun find(code: String): EvChargeSlot? {
        val hit = (cache.get(code) as EvChargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeSlot> = inner.list()
}

fun newCachedEvChargeSlotStore(inner: EvChargeSlotStore, ttl: Int, clock: Clock): EvChargeSlotStore {
    return CachedEvChargeSlotStore(
        inner = inner,
        cache = newTtlCache<EvChargeSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge slots. Empty fields match all. */
data class EvChargeSlotQuery(
    var source: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeSlot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeSlot>): MutableList<EvChargeSlot> {
        val matched = filter(items) { candidate: EvChargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeSlots(store: EvChargeSlotStore, query: EvChargeSlotQuery): MutableList<EvChargeSlot> {
    return query.apply(store.list())
}
