package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization slots by code. */
interface UtilizationSlotStore {
    fun find(code: String): UtilizationSlot?
    fun save(item: UtilizationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationSlotStore(
    var items: MutableMap<String, UtilizationSlot>,
    var order: MutableList<String>,
) : UtilizationSlotStore {
    override fun find(code: String): UtilizationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationSlot) {
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

    override fun list(): MutableList<UtilizationSlot> {
        val out = mutableListOf<UtilizationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationSlotStore(): UtilizationSlotStore {
    return MemoryUtilizationSlotStore(mutableMapOf<String, UtilizationSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationSlotStore(
    var inner: UtilizationSlotStore,
    var cache: TtlCache<UtilizationSlot>,
    var hits: Int,
    var misses: Int,
) : UtilizationSlotStore {
    override fun find(code: String): UtilizationSlot? {
        val hit = (cache.get(code) as UtilizationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationSlot> = inner.list()
}

fun newCachedUtilizationSlotStore(inner: UtilizationSlotStore, ttl: Int, clock: Clock): UtilizationSlotStore {
    return CachedUtilizationSlotStore(
        inner = inner,
        cache = newTtlCache<UtilizationSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization slots. Empty fields match all. */
data class UtilizationSlotQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationSlot): Boolean {
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

    fun apply(items: MutableList<UtilizationSlot>): MutableList<UtilizationSlot> {
        val matched = filter(items) { candidate: UtilizationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationSlots(store: UtilizationSlotStore, query: UtilizationSlotQuery): MutableList<UtilizationSlot> {
    return query.apply(store.list())
}
