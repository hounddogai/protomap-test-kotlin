package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy utilization slots by code. */
interface LegacyUtilizationSlotStore {
    fun find(code: String): LegacyUtilizationSlot?
    fun save(item: LegacyUtilizationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyUtilizationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyUtilizationSlotStore(
    var items: MutableMap<String, LegacyUtilizationSlot>,
    var order: MutableList<String>,
) : LegacyUtilizationSlotStore {
    override fun find(code: String): LegacyUtilizationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyUtilizationSlot) {
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

    override fun list(): MutableList<LegacyUtilizationSlot> {
        val out = mutableListOf<LegacyUtilizationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyUtilizationSlotStore(): LegacyUtilizationSlotStore {
    return MemoryLegacyUtilizationSlotStore(
        mutableMapOf<String, LegacyUtilizationSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyUtilizationSlotStore(
    var inner: LegacyUtilizationSlotStore,
    var cache: TtlCache<LegacyUtilizationSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyUtilizationSlotStore {
    override fun find(code: String): LegacyUtilizationSlot? {
        val hit = (cache.get(code) as LegacyUtilizationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyUtilizationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyUtilizationSlot> = inner.list()
}

fun newCachedLegacyUtilizationSlotStore(inner: LegacyUtilizationSlotStore, ttl: Int, clock: Clock): LegacyUtilizationSlotStore {
    return CachedLegacyUtilizationSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyUtilizationSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy utilization slots. Empty fields match all. */
data class LegacyUtilizationSlotQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyUtilizationSlot): Boolean {
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

    fun apply(items: MutableList<LegacyUtilizationSlot>): MutableList<LegacyUtilizationSlot> {
        val matched = filter(items) { candidate: LegacyUtilizationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyUtilizationSlots(store: LegacyUtilizationSlotStore, query: LegacyUtilizationSlotQuery): MutableList<LegacyUtilizationSlot> {
    return query.apply(store.list())
}
