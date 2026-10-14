package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period assignments by code. */
interface DutyPeriodAssignmentStore {
    fun find(code: String): DutyPeriodAssignment?
    fun save(item: DutyPeriodAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodAssignmentStore(
    var items: MutableMap<String, DutyPeriodAssignment>,
    var order: MutableList<String>,
) : DutyPeriodAssignmentStore {
    override fun find(code: String): DutyPeriodAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodAssignment) {
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

    override fun list(): MutableList<DutyPeriodAssignment> {
        val out = mutableListOf<DutyPeriodAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodAssignmentStore(): DutyPeriodAssignmentStore {
    return MemoryDutyPeriodAssignmentStore(
        mutableMapOf<String, DutyPeriodAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodAssignmentStore(
    var inner: DutyPeriodAssignmentStore,
    var cache: TtlCache<DutyPeriodAssignment>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodAssignmentStore {
    override fun find(code: String): DutyPeriodAssignment? {
        val hit = (cache.get(code) as DutyPeriodAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodAssignment> = inner.list()
}

fun newCachedDutyPeriodAssignmentStore(inner: DutyPeriodAssignmentStore, ttl: Int, clock: Clock): DutyPeriodAssignmentStore {
    return CachedDutyPeriodAssignmentStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period assignments. Empty fields match all. */
data class DutyPeriodAssignmentQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodAssignment>): MutableList<DutyPeriodAssignment> {
        val matched = filter(items) { candidate: DutyPeriodAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodAssignments(store: DutyPeriodAssignmentStore, query: DutyPeriodAssignmentQuery): MutableList<DutyPeriodAssignment> {
    return query.apply(store.list())
}
