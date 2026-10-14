package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period slots by code. */
interface DutyPeriodSlotStore {
    fun find(code: String): DutyPeriodSlot?
    fun save(item: DutyPeriodSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodSlotStore(
    var items: MutableMap<String, DutyPeriodSlot>,
    var order: MutableList<String>,
) : DutyPeriodSlotStore {
    override fun find(code: String): DutyPeriodSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodSlot) {
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

    override fun list(): MutableList<DutyPeriodSlot> {
        val out = mutableListOf<DutyPeriodSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodSlotStore(): DutyPeriodSlotStore {
    return MemoryDutyPeriodSlotStore(mutableMapOf<String, DutyPeriodSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodSlotStore(
    var inner: DutyPeriodSlotStore,
    var cache: TtlCache<DutyPeriodSlot>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodSlotStore {
    override fun find(code: String): DutyPeriodSlot? {
        val hit = (cache.get(code) as DutyPeriodSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodSlot> = inner.list()
}

fun newCachedDutyPeriodSlotStore(inner: DutyPeriodSlotStore, ttl: Int, clock: Clock): DutyPeriodSlotStore {
    return CachedDutyPeriodSlotStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period slots. Empty fields match all. */
data class DutyPeriodSlotQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodSlot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodSlot>): MutableList<DutyPeriodSlot> {
        val matched = filter(items) { candidate: DutyPeriodSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodSlots(store: DutyPeriodSlotStore, query: DutyPeriodSlotQuery): MutableList<DutyPeriodSlot> {
    return query.apply(store.list())
}
