package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict duty period assignments by code. */
interface StrictDutyPeriodAssignmentStore {
    fun find(code: String): StrictDutyPeriodAssignment?
    fun save(item: StrictDutyPeriodAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDutyPeriodAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDutyPeriodAssignmentStore(
    var items: MutableMap<String, StrictDutyPeriodAssignment>,
    var order: MutableList<String>,
) : StrictDutyPeriodAssignmentStore {
    override fun find(code: String): StrictDutyPeriodAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDutyPeriodAssignment) {
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

    override fun list(): MutableList<StrictDutyPeriodAssignment> {
        val out = mutableListOf<StrictDutyPeriodAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDutyPeriodAssignmentStore(): StrictDutyPeriodAssignmentStore {
    return MemoryStrictDutyPeriodAssignmentStore(
        mutableMapOf<String, StrictDutyPeriodAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDutyPeriodAssignmentStore(
    var inner: StrictDutyPeriodAssignmentStore,
    var cache: TtlCache<StrictDutyPeriodAssignment>,
    var hits: Int,
    var misses: Int,
) : StrictDutyPeriodAssignmentStore {
    override fun find(code: String): StrictDutyPeriodAssignment? {
        val hit = (cache.get(code) as StrictDutyPeriodAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDutyPeriodAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDutyPeriodAssignment> = inner.list()
}

fun newCachedStrictDutyPeriodAssignmentStore(inner: StrictDutyPeriodAssignmentStore, ttl: Int, clock: Clock): StrictDutyPeriodAssignmentStore {
    return CachedStrictDutyPeriodAssignmentStore(
        inner = inner,
        cache = newTtlCache<StrictDutyPeriodAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict duty period assignments. Empty fields match all. */
data class StrictDutyPeriodAssignmentQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDutyPeriodAssignment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDutyPeriodAssignment>): MutableList<StrictDutyPeriodAssignment> {
        val matched = filter(items) { candidate: StrictDutyPeriodAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDutyPeriodAssignments(store: StrictDutyPeriodAssignmentStore, query: StrictDutyPeriodAssignmentQuery): MutableList<StrictDutyPeriodAssignment> {
    return query.apply(store.list())
}
