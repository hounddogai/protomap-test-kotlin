package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn assignments by code. */
interface FuelBurnAssignmentStore {
    fun find(code: String): FuelBurnAssignment?
    fun save(item: FuelBurnAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnAssignmentStore(
    var items: MutableMap<String, FuelBurnAssignment>,
    var order: MutableList<String>,
) : FuelBurnAssignmentStore {
    override fun find(code: String): FuelBurnAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnAssignment) {
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

    override fun list(): MutableList<FuelBurnAssignment> {
        val out = mutableListOf<FuelBurnAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnAssignmentStore(): FuelBurnAssignmentStore {
    return MemoryFuelBurnAssignmentStore(mutableMapOf<String, FuelBurnAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnAssignmentStore(
    var inner: FuelBurnAssignmentStore,
    var cache: TtlCache<FuelBurnAssignment>,
    var hits: Int,
    var misses: Int,
) : FuelBurnAssignmentStore {
    override fun find(code: String): FuelBurnAssignment? {
        val hit = (cache.get(code) as FuelBurnAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnAssignment> = inner.list()
}

fun newCachedFuelBurnAssignmentStore(inner: FuelBurnAssignmentStore, ttl: Int, clock: Clock): FuelBurnAssignmentStore {
    return CachedFuelBurnAssignmentStore(
        inner = inner,
        cache = newTtlCache<FuelBurnAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn assignments. Empty fields match all. */
data class FuelBurnAssignmentQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnAssignment>): MutableList<FuelBurnAssignment> {
        val matched = filter(items) { candidate: FuelBurnAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnAssignments(store: FuelBurnAssignmentStore, query: FuelBurnAssignmentQuery): MutableList<FuelBurnAssignment> {
    return query.apply(store.list())
}
