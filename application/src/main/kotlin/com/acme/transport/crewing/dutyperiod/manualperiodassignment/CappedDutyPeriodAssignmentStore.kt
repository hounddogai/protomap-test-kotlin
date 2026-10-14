package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped duty period assignments by code. */
interface CappedDutyPeriodAssignmentStore {
    fun find(code: String): CappedDutyPeriodAssignment?
    fun save(item: CappedDutyPeriodAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDutyPeriodAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDutyPeriodAssignmentStore(
    var items: MutableMap<String, CappedDutyPeriodAssignment>,
    var order: MutableList<String>,
) : CappedDutyPeriodAssignmentStore {
    override fun find(code: String): CappedDutyPeriodAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDutyPeriodAssignment) {
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

    override fun list(): MutableList<CappedDutyPeriodAssignment> {
        val out = mutableListOf<CappedDutyPeriodAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDutyPeriodAssignmentStore(): CappedDutyPeriodAssignmentStore {
    return MemoryCappedDutyPeriodAssignmentStore(
        mutableMapOf<String, CappedDutyPeriodAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDutyPeriodAssignmentStore(
    var inner: CappedDutyPeriodAssignmentStore,
    var cache: TtlCache<CappedDutyPeriodAssignment>,
    var hits: Int,
    var misses: Int,
) : CappedDutyPeriodAssignmentStore {
    override fun find(code: String): CappedDutyPeriodAssignment? {
        val hit = (cache.get(code) as CappedDutyPeriodAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDutyPeriodAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDutyPeriodAssignment> = inner.list()
}

fun newCachedCappedDutyPeriodAssignmentStore(inner: CappedDutyPeriodAssignmentStore, ttl: Int, clock: Clock): CappedDutyPeriodAssignmentStore {
    return CachedCappedDutyPeriodAssignmentStore(
        inner = inner,
        cache = newTtlCache<CappedDutyPeriodAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped duty period assignments. Empty fields match all. */
data class CappedDutyPeriodAssignmentQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDutyPeriodAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDutyPeriodAssignment>): MutableList<CappedDutyPeriodAssignment> {
        val matched = filter(items) { candidate: CappedDutyPeriodAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDutyPeriodAssignments(store: CappedDutyPeriodAssignmentStore, query: CappedDutyPeriodAssignmentQuery): MutableList<CappedDutyPeriodAssignment> {
    return query.apply(store.list())
}
