package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive lease return schedules by code. */
interface AdaptiveLeaseReturnScheduleStore {
    fun find(code: String): AdaptiveLeaseReturnSchedule?
    fun save(item: AdaptiveLeaseReturnSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLeaseReturnSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLeaseReturnScheduleStore(
    var items: MutableMap<String, AdaptiveLeaseReturnSchedule>,
    var order: MutableList<String>,
) : AdaptiveLeaseReturnScheduleStore {
    override fun find(code: String): AdaptiveLeaseReturnSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLeaseReturnSchedule) {
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

    override fun list(): MutableList<AdaptiveLeaseReturnSchedule> {
        val out = mutableListOf<AdaptiveLeaseReturnSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLeaseReturnScheduleStore(): AdaptiveLeaseReturnScheduleStore {
    return MemoryAdaptiveLeaseReturnScheduleStore(
        mutableMapOf<String, AdaptiveLeaseReturnSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLeaseReturnScheduleStore(
    var inner: AdaptiveLeaseReturnScheduleStore,
    var cache: TtlCache<AdaptiveLeaseReturnSchedule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLeaseReturnScheduleStore {
    override fun find(code: String): AdaptiveLeaseReturnSchedule? {
        val hit = (cache.get(code) as AdaptiveLeaseReturnSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLeaseReturnSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLeaseReturnSchedule> = inner.list()
}

fun newCachedAdaptiveLeaseReturnScheduleStore(inner: AdaptiveLeaseReturnScheduleStore, ttl: Int, clock: Clock): AdaptiveLeaseReturnScheduleStore {
    return CachedAdaptiveLeaseReturnScheduleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLeaseReturnSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lease return schedules. Empty fields match all. */
data class AdaptiveLeaseReturnScheduleQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLeaseReturnSchedule): Boolean {
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

    fun apply(items: MutableList<AdaptiveLeaseReturnSchedule>): MutableList<AdaptiveLeaseReturnSchedule> {
        val matched = filter(items) { candidate: AdaptiveLeaseReturnSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLeaseReturnSchedules(store: AdaptiveLeaseReturnScheduleStore, query: AdaptiveLeaseReturnScheduleQuery): MutableList<AdaptiveLeaseReturnSchedule> {
    return query.apply(store.list())
}
