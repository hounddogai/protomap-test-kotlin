package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional redelivery assignments by code. */
interface RegionalRedeliveryAssignmentStore {
    fun find(code: String): RegionalRedeliveryAssignment?
    fun save(item: RegionalRedeliveryAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRedeliveryAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRedeliveryAssignmentStore(
    var items: MutableMap<String, RegionalRedeliveryAssignment>,
    var order: MutableList<String>,
) : RegionalRedeliveryAssignmentStore {
    override fun find(code: String): RegionalRedeliveryAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRedeliveryAssignment) {
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

    override fun list(): MutableList<RegionalRedeliveryAssignment> {
        val out = mutableListOf<RegionalRedeliveryAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRedeliveryAssignmentStore(): RegionalRedeliveryAssignmentStore {
    return MemoryRegionalRedeliveryAssignmentStore(
        mutableMapOf<String, RegionalRedeliveryAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRedeliveryAssignmentStore(
    var inner: RegionalRedeliveryAssignmentStore,
    var cache: TtlCache<RegionalRedeliveryAssignment>,
    var hits: Int,
    var misses: Int,
) : RegionalRedeliveryAssignmentStore {
    override fun find(code: String): RegionalRedeliveryAssignment? {
        val hit = (cache.get(code) as RegionalRedeliveryAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRedeliveryAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRedeliveryAssignment> = inner.list()
}

fun newCachedRegionalRedeliveryAssignmentStore(inner: RegionalRedeliveryAssignmentStore, ttl: Int, clock: Clock): RegionalRedeliveryAssignmentStore {
    return CachedRegionalRedeliveryAssignmentStore(
        inner = inner,
        cache = newTtlCache<RegionalRedeliveryAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional redelivery assignments. Empty fields match all. */
data class RegionalRedeliveryAssignmentQuery(
    var unit: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRedeliveryAssignment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRedeliveryAssignment>): MutableList<RegionalRedeliveryAssignment> {
        val matched = filter(items) { candidate: RegionalRedeliveryAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRedeliveryAssignments(store: RegionalRedeliveryAssignmentStore, query: RegionalRedeliveryAssignmentQuery): MutableList<RegionalRedeliveryAssignment> {
    return query.apply(store.list())
}
