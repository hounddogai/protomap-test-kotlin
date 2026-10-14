package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped lease return schedules by code. */
interface CappedLeaseReturnScheduleStore {
    fun find(code: String): CappedLeaseReturnSchedule?
    fun save(item: CappedLeaseReturnSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLeaseReturnSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLeaseReturnScheduleStore(
    var items: MutableMap<String, CappedLeaseReturnSchedule>,
    var order: MutableList<String>,
) : CappedLeaseReturnScheduleStore {
    override fun find(code: String): CappedLeaseReturnSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLeaseReturnSchedule) {
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

    override fun list(): MutableList<CappedLeaseReturnSchedule> {
        val out = mutableListOf<CappedLeaseReturnSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLeaseReturnScheduleStore(): CappedLeaseReturnScheduleStore {
    return MemoryCappedLeaseReturnScheduleStore(
        mutableMapOf<String, CappedLeaseReturnSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLeaseReturnScheduleStore(
    var inner: CappedLeaseReturnScheduleStore,
    var cache: TtlCache<CappedLeaseReturnSchedule>,
    var hits: Int,
    var misses: Int,
) : CappedLeaseReturnScheduleStore {
    override fun find(code: String): CappedLeaseReturnSchedule? {
        val hit = (cache.get(code) as CappedLeaseReturnSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLeaseReturnSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLeaseReturnSchedule> = inner.list()
}

fun newCachedCappedLeaseReturnScheduleStore(inner: CappedLeaseReturnScheduleStore, ttl: Int, clock: Clock): CappedLeaseReturnScheduleStore {
    return CachedCappedLeaseReturnScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedLeaseReturnSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped lease return schedules. Empty fields match all. */
data class CappedLeaseReturnScheduleQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLeaseReturnSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLeaseReturnSchedule>): MutableList<CappedLeaseReturnSchedule> {
        val matched = filter(items) { candidate: CappedLeaseReturnSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLeaseReturnSchedules(store: CappedLeaseReturnScheduleStore, query: CappedLeaseReturnScheduleQuery): MutableList<CappedLeaseReturnSchedule> {
    return query.apply(store.list())
}
