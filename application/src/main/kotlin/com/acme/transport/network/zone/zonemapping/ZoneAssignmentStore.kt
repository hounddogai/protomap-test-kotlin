package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone assignments by code. */
interface ZoneAssignmentStore {
    fun find(code: String): ZoneAssignment?
    fun save(item: ZoneAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneAssignmentStore(
    var items: MutableMap<String, ZoneAssignment>,
    var order: MutableList<String>,
) : ZoneAssignmentStore {
    override fun find(code: String): ZoneAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneAssignment) {
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

    override fun list(): MutableList<ZoneAssignment> {
        val out = mutableListOf<ZoneAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneAssignmentStore(): ZoneAssignmentStore {
    return MemoryZoneAssignmentStore(mutableMapOf<String, ZoneAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneAssignmentStore(
    var inner: ZoneAssignmentStore,
    var cache: TtlCache<ZoneAssignment>,
    var hits: Int,
    var misses: Int,
) : ZoneAssignmentStore {
    override fun find(code: String): ZoneAssignment? {
        val hit = (cache.get(code) as ZoneAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneAssignment> = inner.list()
}

fun newCachedZoneAssignmentStore(inner: ZoneAssignmentStore, ttl: Int, clock: Clock): ZoneAssignmentStore {
    return CachedZoneAssignmentStore(
        inner = inner,
        cache = newTtlCache<ZoneAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone assignments. Empty fields match all. */
data class ZoneAssignmentQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneAssignment>): MutableList<ZoneAssignment> {
        val matched = filter(items) { candidate: ZoneAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneAssignments(store: ZoneAssignmentStore, query: ZoneAssignmentQuery): MutableList<ZoneAssignment> {
    return query.apply(store.list())
}
