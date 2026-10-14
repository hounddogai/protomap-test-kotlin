package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary lease return schedules by code. */
interface PrimaryLeaseReturnScheduleStore {
    fun find(code: String): PrimaryLeaseReturnSchedule?
    fun save(item: PrimaryLeaseReturnSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLeaseReturnSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLeaseReturnScheduleStore(
    var items: MutableMap<String, PrimaryLeaseReturnSchedule>,
    var order: MutableList<String>,
) : PrimaryLeaseReturnScheduleStore {
    override fun find(code: String): PrimaryLeaseReturnSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLeaseReturnSchedule) {
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

    override fun list(): MutableList<PrimaryLeaseReturnSchedule> {
        val out = mutableListOf<PrimaryLeaseReturnSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLeaseReturnScheduleStore(): PrimaryLeaseReturnScheduleStore {
    return MemoryPrimaryLeaseReturnScheduleStore(
        mutableMapOf<String, PrimaryLeaseReturnSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLeaseReturnScheduleStore(
    var inner: PrimaryLeaseReturnScheduleStore,
    var cache: TtlCache<PrimaryLeaseReturnSchedule>,
    var hits: Int,
    var misses: Int,
) : PrimaryLeaseReturnScheduleStore {
    override fun find(code: String): PrimaryLeaseReturnSchedule? {
        val hit = (cache.get(code) as PrimaryLeaseReturnSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLeaseReturnSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLeaseReturnSchedule> = inner.list()
}

fun newCachedPrimaryLeaseReturnScheduleStore(inner: PrimaryLeaseReturnScheduleStore, ttl: Int, clock: Clock): PrimaryLeaseReturnScheduleStore {
    return CachedPrimaryLeaseReturnScheduleStore(
        inner = inner,
        cache = newTtlCache<PrimaryLeaseReturnSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary lease return schedules. Empty fields match all. */
data class PrimaryLeaseReturnScheduleQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLeaseReturnSchedule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<PrimaryLeaseReturnSchedule>): MutableList<PrimaryLeaseReturnSchedule> {
        val matched = filter(items) { candidate: PrimaryLeaseReturnSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLeaseReturnSchedules(store: PrimaryLeaseReturnScheduleStore, query: PrimaryLeaseReturnScheduleQuery): MutableList<PrimaryLeaseReturnSchedule> {
    return query.apply(store.list())
}
