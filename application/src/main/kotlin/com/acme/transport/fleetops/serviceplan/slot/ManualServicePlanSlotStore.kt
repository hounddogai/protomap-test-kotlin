package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan slots by code. */
interface ManualServicePlanSlotStore {
    fun find(code: String): ManualServicePlanSlot?
    fun save(item: ManualServicePlanSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanSlotStore(
    var items: MutableMap<String, ManualServicePlanSlot>,
    var order: MutableList<String>,
) : ManualServicePlanSlotStore {
    override fun find(code: String): ManualServicePlanSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanSlot) {
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

    override fun list(): MutableList<ManualServicePlanSlot> {
        val out = mutableListOf<ManualServicePlanSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanSlotStore(): ManualServicePlanSlotStore {
    return MemoryManualServicePlanSlotStore(
        mutableMapOf<String, ManualServicePlanSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanSlotStore(
    var inner: ManualServicePlanSlotStore,
    var cache: TtlCache<ManualServicePlanSlot>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanSlotStore {
    override fun find(code: String): ManualServicePlanSlot? {
        val hit = (cache.get(code) as ManualServicePlanSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanSlot> = inner.list()
}

fun newCachedManualServicePlanSlotStore(inner: ManualServicePlanSlotStore, ttl: Int, clock: Clock): ManualServicePlanSlotStore {
    return CachedManualServicePlanSlotStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan slots. Empty fields match all. */
data class ManualServicePlanSlotQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanSlot>): MutableList<ManualServicePlanSlot> {
        val matched = filter(items) { candidate: ManualServicePlanSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanSlots(store: ManualServicePlanSlotStore, query: ManualServicePlanSlotQuery): MutableList<ManualServicePlanSlot> {
    return query.apply(store.list())
}
