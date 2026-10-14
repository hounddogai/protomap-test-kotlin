package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual transit time slots by code. */
interface ManualTransitTimeSlotStore {
    fun find(code: String): ManualTransitTimeSlot?
    fun save(item: ManualTransitTimeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTransitTimeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTransitTimeSlotStore(
    var items: MutableMap<String, ManualTransitTimeSlot>,
    var order: MutableList<String>,
) : ManualTransitTimeSlotStore {
    override fun find(code: String): ManualTransitTimeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTransitTimeSlot) {
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

    override fun list(): MutableList<ManualTransitTimeSlot> {
        val out = mutableListOf<ManualTransitTimeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTransitTimeSlotStore(): ManualTransitTimeSlotStore {
    return MemoryManualTransitTimeSlotStore(
        mutableMapOf<String, ManualTransitTimeSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTransitTimeSlotStore(
    var inner: ManualTransitTimeSlotStore,
    var cache: TtlCache<ManualTransitTimeSlot>,
    var hits: Int,
    var misses: Int,
) : ManualTransitTimeSlotStore {
    override fun find(code: String): ManualTransitTimeSlot? {
        val hit = (cache.get(code) as ManualTransitTimeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTransitTimeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTransitTimeSlot> = inner.list()
}

fun newCachedManualTransitTimeSlotStore(inner: ManualTransitTimeSlotStore, ttl: Int, clock: Clock): ManualTransitTimeSlotStore {
    return CachedManualTransitTimeSlotStore(
        inner = inner,
        cache = newTtlCache<ManualTransitTimeSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual transit time slots. Empty fields match all. */
data class ManualTransitTimeSlotQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTransitTimeSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTransitTimeSlot>): MutableList<ManualTransitTimeSlot> {
        val matched = filter(items) { candidate: ManualTransitTimeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTransitTimeSlots(store: ManualTransitTimeSlotStore, query: ManualTransitTimeSlotQuery): MutableList<ManualTransitTimeSlot> {
    return query.apply(store.list())
}
