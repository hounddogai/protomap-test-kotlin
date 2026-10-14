package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report assignments by code. */
interface RegionalCarbonReportAssignmentStore {
    fun find(code: String): RegionalCarbonReportAssignment?
    fun save(item: RegionalCarbonReportAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportAssignmentStore(
    var items: MutableMap<String, RegionalCarbonReportAssignment>,
    var order: MutableList<String>,
) : RegionalCarbonReportAssignmentStore {
    override fun find(code: String): RegionalCarbonReportAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportAssignment) {
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

    override fun list(): MutableList<RegionalCarbonReportAssignment> {
        val out = mutableListOf<RegionalCarbonReportAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportAssignmentStore(): RegionalCarbonReportAssignmentStore {
    return MemoryRegionalCarbonReportAssignmentStore(
        mutableMapOf<String, RegionalCarbonReportAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportAssignmentStore(
    var inner: RegionalCarbonReportAssignmentStore,
    var cache: TtlCache<RegionalCarbonReportAssignment>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportAssignmentStore {
    override fun find(code: String): RegionalCarbonReportAssignment? {
        val hit = (cache.get(code) as RegionalCarbonReportAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportAssignment> = inner.list()
}

fun newCachedRegionalCarbonReportAssignmentStore(inner: RegionalCarbonReportAssignmentStore, ttl: Int, clock: Clock): RegionalCarbonReportAssignmentStore {
    return CachedRegionalCarbonReportAssignmentStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report assignments. Empty fields match all. */
data class RegionalCarbonReportAssignmentQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportAssignment>): MutableList<RegionalCarbonReportAssignment> {
        val matched = filter(items) { candidate: RegionalCarbonReportAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportAssignments(store: RegionalCarbonReportAssignmentStore, query: RegionalCarbonReportAssignmentQuery): MutableList<RegionalCarbonReportAssignment> {
    return query.apply(store.list())
}
