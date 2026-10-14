package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy route offset assignments by code. */
interface LegacyRouteOffsetAssignmentStore {
    fun find(code: String): LegacyRouteOffsetAssignment?
    fun save(item: LegacyRouteOffsetAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRouteOffsetAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRouteOffsetAssignmentStore(
    var items: MutableMap<String, LegacyRouteOffsetAssignment>,
    var order: MutableList<String>,
) : LegacyRouteOffsetAssignmentStore {
    override fun find(code: String): LegacyRouteOffsetAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRouteOffsetAssignment) {
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

    override fun list(): MutableList<LegacyRouteOffsetAssignment> {
        val out = mutableListOf<LegacyRouteOffsetAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRouteOffsetAssignmentStore(): LegacyRouteOffsetAssignmentStore {
    return MemoryLegacyRouteOffsetAssignmentStore(
        mutableMapOf<String, LegacyRouteOffsetAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRouteOffsetAssignmentStore(
    var inner: LegacyRouteOffsetAssignmentStore,
    var cache: TtlCache<LegacyRouteOffsetAssignment>,
    var hits: Int,
    var misses: Int,
) : LegacyRouteOffsetAssignmentStore {
    override fun find(code: String): LegacyRouteOffsetAssignment? {
        val hit = (cache.get(code) as LegacyRouteOffsetAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRouteOffsetAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRouteOffsetAssignment> = inner.list()
}

fun newCachedLegacyRouteOffsetAssignmentStore(inner: LegacyRouteOffsetAssignmentStore, ttl: Int, clock: Clock): LegacyRouteOffsetAssignmentStore {
    return CachedLegacyRouteOffsetAssignmentStore(
        inner = inner,
        cache = newTtlCache<LegacyRouteOffsetAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy route offset assignments. Empty fields match all. */
data class LegacyRouteOffsetAssignmentQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRouteOffsetAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRouteOffsetAssignment>): MutableList<LegacyRouteOffsetAssignment> {
        val matched = filter(items) { candidate: LegacyRouteOffsetAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRouteOffsetAssignments(store: LegacyRouteOffsetAssignmentStore, query: LegacyRouteOffsetAssignmentQuery): MutableList<LegacyRouteOffsetAssignment> {
    return query.apply(store.list())
}
