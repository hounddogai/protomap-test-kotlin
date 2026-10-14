package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling carbon report assignments by code. */
interface RollingCarbonReportAssignmentStore {
    fun find(code: String): RollingCarbonReportAssignment?
    fun save(item: RollingCarbonReportAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCarbonReportAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCarbonReportAssignmentStore(
    var items: MutableMap<String, RollingCarbonReportAssignment>,
    var order: MutableList<String>,
) : RollingCarbonReportAssignmentStore {
    override fun find(code: String): RollingCarbonReportAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCarbonReportAssignment) {
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

    override fun list(): MutableList<RollingCarbonReportAssignment> {
        val out = mutableListOf<RollingCarbonReportAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCarbonReportAssignmentStore(): RollingCarbonReportAssignmentStore {
    return MemoryRollingCarbonReportAssignmentStore(
        mutableMapOf<String, RollingCarbonReportAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCarbonReportAssignmentStore(
    var inner: RollingCarbonReportAssignmentStore,
    var cache: TtlCache<RollingCarbonReportAssignment>,
    var hits: Int,
    var misses: Int,
) : RollingCarbonReportAssignmentStore {
    override fun find(code: String): RollingCarbonReportAssignment? {
        val hit = (cache.get(code) as RollingCarbonReportAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCarbonReportAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCarbonReportAssignment> = inner.list()
}

fun newCachedRollingCarbonReportAssignmentStore(inner: RollingCarbonReportAssignmentStore, ttl: Int, clock: Clock): RollingCarbonReportAssignmentStore {
    return CachedRollingCarbonReportAssignmentStore(
        inner = inner,
        cache = newTtlCache<RollingCarbonReportAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling carbon report assignments. Empty fields match all. */
data class RollingCarbonReportAssignmentQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCarbonReportAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCarbonReportAssignment>): MutableList<RollingCarbonReportAssignment> {
        val matched = filter(items) { candidate: RollingCarbonReportAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCarbonReportAssignments(store: RollingCarbonReportAssignmentStore, query: RollingCarbonReportAssignmentQuery): MutableList<RollingCarbonReportAssignment> {
    return query.apply(store.list())
}
