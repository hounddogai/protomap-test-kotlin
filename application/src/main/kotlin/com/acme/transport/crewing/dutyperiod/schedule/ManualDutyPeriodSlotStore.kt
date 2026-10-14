package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual duty period slots by code. */
interface ManualDutyPeriodSlotStore {
    fun find(code: String): ManualDutyPeriodSlot?
    fun save(item: ManualDutyPeriodSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDutyPeriodSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDutyPeriodSlotStore(
    var items: MutableMap<String, ManualDutyPeriodSlot>,
    var order: MutableList<String>,
) : ManualDutyPeriodSlotStore {
    override fun find(code: String): ManualDutyPeriodSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDutyPeriodSlot) {
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

    override fun list(): MutableList<ManualDutyPeriodSlot> {
        val out = mutableListOf<ManualDutyPeriodSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDutyPeriodSlotStore(): ManualDutyPeriodSlotStore {
    return MemoryManualDutyPeriodSlotStore(
        mutableMapOf<String, ManualDutyPeriodSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDutyPeriodSlotStore(
    var inner: ManualDutyPeriodSlotStore,
    var cache: TtlCache<ManualDutyPeriodSlot>,
    var hits: Int,
    var misses: Int,
) : ManualDutyPeriodSlotStore {
    override fun find(code: String): ManualDutyPeriodSlot? {
        val hit = (cache.get(code) as ManualDutyPeriodSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDutyPeriodSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDutyPeriodSlot> = inner.list()
}

fun newCachedManualDutyPeriodSlotStore(inner: ManualDutyPeriodSlotStore, ttl: Int, clock: Clock): ManualDutyPeriodSlotStore {
    return CachedManualDutyPeriodSlotStore(
        inner = inner,
        cache = newTtlCache<ManualDutyPeriodSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual duty period slots. Empty fields match all. */
data class ManualDutyPeriodSlotQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDutyPeriodSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDutyPeriodSlot>): MutableList<ManualDutyPeriodSlot> {
        val matched = filter(items) { candidate: ManualDutyPeriodSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDutyPeriodSlots(store: ManualDutyPeriodSlotStore, query: ManualDutyPeriodSlotQuery): MutableList<ManualDutyPeriodSlot> {
    return query.apply(store.list())
}
