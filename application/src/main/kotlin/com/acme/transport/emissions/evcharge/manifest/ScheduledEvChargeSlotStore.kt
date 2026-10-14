package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled ev charge slots by code. */
interface ScheduledEvChargeSlotStore {
    fun find(code: String): ScheduledEvChargeSlot?
    fun save(item: ScheduledEvChargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEvChargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEvChargeSlotStore(
    var items: MutableMap<String, ScheduledEvChargeSlot>,
    var order: MutableList<String>,
) : ScheduledEvChargeSlotStore {
    override fun find(code: String): ScheduledEvChargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEvChargeSlot) {
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

    override fun list(): MutableList<ScheduledEvChargeSlot> {
        val out = mutableListOf<ScheduledEvChargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEvChargeSlotStore(): ScheduledEvChargeSlotStore {
    return MemoryScheduledEvChargeSlotStore(
        mutableMapOf<String, ScheduledEvChargeSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEvChargeSlotStore(
    var inner: ScheduledEvChargeSlotStore,
    var cache: TtlCache<ScheduledEvChargeSlot>,
    var hits: Int,
    var misses: Int,
) : ScheduledEvChargeSlotStore {
    override fun find(code: String): ScheduledEvChargeSlot? {
        val hit = (cache.get(code) as ScheduledEvChargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEvChargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEvChargeSlot> = inner.list()
}

fun newCachedScheduledEvChargeSlotStore(inner: ScheduledEvChargeSlotStore, ttl: Int, clock: Clock): ScheduledEvChargeSlotStore {
    return CachedScheduledEvChargeSlotStore(
        inner = inner,
        cache = newTtlCache<ScheduledEvChargeSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled ev charge slots. Empty fields match all. */
data class ScheduledEvChargeSlotQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEvChargeSlot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledEvChargeSlot>): MutableList<ScheduledEvChargeSlot> {
        val matched = filter(items) { candidate: ScheduledEvChargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEvChargeSlots(store: ScheduledEvChargeSlotStore, query: ScheduledEvChargeSlotQuery): MutableList<ScheduledEvChargeSlot> {
    return query.apply(store.list())
}
