package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity slots by code. */
interface CapacitySlotStore {
    fun find(code: String): CapacitySlot?
    fun save(item: CapacitySlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacitySlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacitySlotStore(
    var items: MutableMap<String, CapacitySlot>,
    var order: MutableList<String>,
) : CapacitySlotStore {
    override fun find(code: String): CapacitySlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacitySlot) {
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

    override fun list(): MutableList<CapacitySlot> {
        val out = mutableListOf<CapacitySlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacitySlotStore(): CapacitySlotStore {
    return MemoryCapacitySlotStore(mutableMapOf<String, CapacitySlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacitySlotStore(
    var inner: CapacitySlotStore,
    var cache: TtlCache<CapacitySlot>,
    var hits: Int,
    var misses: Int,
) : CapacitySlotStore {
    override fun find(code: String): CapacitySlot? {
        val hit = (cache.get(code) as CapacitySlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacitySlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacitySlot> = inner.list()
}

fun newCachedCapacitySlotStore(inner: CapacitySlotStore, ttl: Int, clock: Clock): CapacitySlotStore {
    return CachedCapacitySlotStore(
        inner = inner,
        cache = newTtlCache<CapacitySlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity slots. Empty fields match all. */
data class CapacitySlotQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacitySlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacitySlot>): MutableList<CapacitySlot> {
        val matched = filter(items) { candidate: CapacitySlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacitySlots(store: CapacitySlotStore, query: CapacitySlotQuery): MutableList<CapacitySlot> {
    return query.apply(store.list())
}
