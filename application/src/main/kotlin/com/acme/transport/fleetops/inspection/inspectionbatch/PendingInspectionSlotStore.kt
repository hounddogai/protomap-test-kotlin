package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection slots by code. */
interface PendingInspectionSlotStore {
    fun find(code: String): PendingInspectionSlot?
    fun save(item: PendingInspectionSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionSlotStore(
    var items: MutableMap<String, PendingInspectionSlot>,
    var order: MutableList<String>,
) : PendingInspectionSlotStore {
    override fun find(code: String): PendingInspectionSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionSlot) {
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

    override fun list(): MutableList<PendingInspectionSlot> {
        val out = mutableListOf<PendingInspectionSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionSlotStore(): PendingInspectionSlotStore {
    return MemoryPendingInspectionSlotStore(
        mutableMapOf<String, PendingInspectionSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionSlotStore(
    var inner: PendingInspectionSlotStore,
    var cache: TtlCache<PendingInspectionSlot>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionSlotStore {
    override fun find(code: String): PendingInspectionSlot? {
        val hit = (cache.get(code) as PendingInspectionSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionSlot> = inner.list()
}

fun newCachedPendingInspectionSlotStore(inner: PendingInspectionSlotStore, ttl: Int, clock: Clock): PendingInspectionSlotStore {
    return CachedPendingInspectionSlotStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection slots. Empty fields match all. */
data class PendingInspectionSlotQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionSlot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionSlot>): MutableList<PendingInspectionSlot> {
        val matched = filter(items) { candidate: PendingInspectionSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionSlots(store: PendingInspectionSlotStore, query: PendingInspectionSlotQuery): MutableList<PendingInspectionSlot> {
    return query.apply(store.list())
}
