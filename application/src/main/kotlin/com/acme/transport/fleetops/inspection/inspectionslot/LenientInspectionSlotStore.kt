package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient inspection slots by code. */
interface LenientInspectionSlotStore {
    fun find(code: String): LenientInspectionSlot?
    fun save(item: LenientInspectionSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientInspectionSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientInspectionSlotStore(
    var items: MutableMap<String, LenientInspectionSlot>,
    var order: MutableList<String>,
) : LenientInspectionSlotStore {
    override fun find(code: String): LenientInspectionSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientInspectionSlot) {
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

    override fun list(): MutableList<LenientInspectionSlot> {
        val out = mutableListOf<LenientInspectionSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientInspectionSlotStore(): LenientInspectionSlotStore {
    return MemoryLenientInspectionSlotStore(
        mutableMapOf<String, LenientInspectionSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientInspectionSlotStore(
    var inner: LenientInspectionSlotStore,
    var cache: TtlCache<LenientInspectionSlot>,
    var hits: Int,
    var misses: Int,
) : LenientInspectionSlotStore {
    override fun find(code: String): LenientInspectionSlot? {
        val hit = (cache.get(code) as LenientInspectionSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientInspectionSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientInspectionSlot> = inner.list()
}

fun newCachedLenientInspectionSlotStore(inner: LenientInspectionSlotStore, ttl: Int, clock: Clock): LenientInspectionSlotStore {
    return CachedLenientInspectionSlotStore(
        inner = inner,
        cache = newTtlCache<LenientInspectionSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient inspection slots. Empty fields match all. */
data class LenientInspectionSlotQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientInspectionSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientInspectionSlot>): MutableList<LenientInspectionSlot> {
        val matched = filter(items) { candidate: LenientInspectionSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientInspectionSlots(store: LenientInspectionSlotStore, query: LenientInspectionSlotQuery): MutableList<LenientInspectionSlot> {
    return query.apply(store.list())
}
