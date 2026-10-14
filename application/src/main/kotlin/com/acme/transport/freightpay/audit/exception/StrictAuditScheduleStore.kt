package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict audit schedules by code. */
interface StrictAuditScheduleStore {
    fun find(code: String): StrictAuditSchedule?
    fun save(item: StrictAuditSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAuditSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAuditScheduleStore(
    var items: MutableMap<String, StrictAuditSchedule>,
    var order: MutableList<String>,
) : StrictAuditScheduleStore {
    override fun find(code: String): StrictAuditSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAuditSchedule) {
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

    override fun list(): MutableList<StrictAuditSchedule> {
        val out = mutableListOf<StrictAuditSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAuditScheduleStore(): StrictAuditScheduleStore {
    return MemoryStrictAuditScheduleStore(mutableMapOf<String, StrictAuditSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAuditScheduleStore(
    var inner: StrictAuditScheduleStore,
    var cache: TtlCache<StrictAuditSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictAuditScheduleStore {
    override fun find(code: String): StrictAuditSchedule? {
        val hit = (cache.get(code) as StrictAuditSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAuditSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAuditSchedule> = inner.list()
}

fun newCachedStrictAuditScheduleStore(inner: StrictAuditScheduleStore, ttl: Int, clock: Clock): StrictAuditScheduleStore {
    return CachedStrictAuditScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictAuditSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict audit schedules. Empty fields match all. */
data class StrictAuditScheduleQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAuditSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAuditSchedule>): MutableList<StrictAuditSchedule> {
        val matched = filter(items) { candidate: StrictAuditSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAuditSchedules(store: StrictAuditScheduleStore, query: StrictAuditScheduleQuery): MutableList<StrictAuditSchedule> {
    return query.apply(store.list())
}
