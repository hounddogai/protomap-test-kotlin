package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report assignments by code. */
interface CarbonReportAssignmentStore {
    fun find(code: String): CarbonReportAssignment?
    fun save(item: CarbonReportAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportAssignmentStore(
    var items: MutableMap<String, CarbonReportAssignment>,
    var order: MutableList<String>,
) : CarbonReportAssignmentStore {
    override fun find(code: String): CarbonReportAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportAssignment) {
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

    override fun list(): MutableList<CarbonReportAssignment> {
        val out = mutableListOf<CarbonReportAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportAssignmentStore(): CarbonReportAssignmentStore {
    return MemoryCarbonReportAssignmentStore(
        mutableMapOf<String, CarbonReportAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportAssignmentStore(
    var inner: CarbonReportAssignmentStore,
    var cache: TtlCache<CarbonReportAssignment>,
    var hits: Int,
    var misses: Int,
) : CarbonReportAssignmentStore {
    override fun find(code: String): CarbonReportAssignment? {
        val hit = (cache.get(code) as CarbonReportAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportAssignment> = inner.list()
}

fun newCachedCarbonReportAssignmentStore(inner: CarbonReportAssignmentStore, ttl: Int, clock: Clock): CarbonReportAssignmentStore {
    return CachedCarbonReportAssignmentStore(
        inner = inner,
        cache = newTtlCache<CarbonReportAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report assignments. Empty fields match all. */
data class CarbonReportAssignmentQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportAssignment): Boolean {
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

    fun apply(items: MutableList<CarbonReportAssignment>): MutableList<CarbonReportAssignment> {
        val matched = filter(items) { candidate: CarbonReportAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportAssignments(store: CarbonReportAssignmentStore, query: CarbonReportAssignmentQuery): MutableList<CarbonReportAssignment> {
    return query.apply(store.list())
}
