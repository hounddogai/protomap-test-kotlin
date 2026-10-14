package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending defect slots by code. */
interface PendingDefectSlotStore {
    fun find(code: String): PendingDefectSlot?
    fun save(item: PendingDefectSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDefectSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDefectSlotStore(
    var items: MutableMap<String, PendingDefectSlot>,
    var order: MutableList<String>,
) : PendingDefectSlotStore {
    override fun find(code: String): PendingDefectSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDefectSlot) {
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

    override fun list(): MutableList<PendingDefectSlot> {
        val out = mutableListOf<PendingDefectSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDefectSlotStore(): PendingDefectSlotStore {
    return MemoryPendingDefectSlotStore(mutableMapOf<String, PendingDefectSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDefectSlotStore(
    var inner: PendingDefectSlotStore,
    var cache: TtlCache<PendingDefectSlot>,
    var hits: Int,
    var misses: Int,
) : PendingDefectSlotStore {
    override fun find(code: String): PendingDefectSlot? {
        val hit = (cache.get(code) as PendingDefectSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDefectSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDefectSlot> = inner.list()
}

fun newCachedPendingDefectSlotStore(inner: PendingDefectSlotStore, ttl: Int, clock: Clock): PendingDefectSlotStore {
    return CachedPendingDefectSlotStore(
        inner = inner,
        cache = newTtlCache<PendingDefectSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending defect slots. Empty fields match all. */
data class PendingDefectSlotQuery(
    var channel: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDefectSlot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDefectSlot>): MutableList<PendingDefectSlot> {
        val matched = filter(items) { candidate: PendingDefectSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDefectSlots(store: PendingDefectSlotStore, query: PendingDefectSlotQuery): MutableList<PendingDefectSlot> {
    return query.apply(store.list())
}
