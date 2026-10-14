package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default fuel burn assignments by code. */
interface DefaultFuelBurnAssignmentStore {
    fun find(code: String): DefaultFuelBurnAssignment?
    fun save(item: DefaultFuelBurnAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultFuelBurnAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultFuelBurnAssignmentStore(
    var items: MutableMap<String, DefaultFuelBurnAssignment>,
    var order: MutableList<String>,
) : DefaultFuelBurnAssignmentStore {
    override fun find(code: String): DefaultFuelBurnAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultFuelBurnAssignment) {
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

    override fun list(): MutableList<DefaultFuelBurnAssignment> {
        val out = mutableListOf<DefaultFuelBurnAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultFuelBurnAssignmentStore(): DefaultFuelBurnAssignmentStore {
    return MemoryDefaultFuelBurnAssignmentStore(
        mutableMapOf<String, DefaultFuelBurnAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultFuelBurnAssignmentStore(
    var inner: DefaultFuelBurnAssignmentStore,
    var cache: TtlCache<DefaultFuelBurnAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultFuelBurnAssignmentStore {
    override fun find(code: String): DefaultFuelBurnAssignment? {
        val hit = (cache.get(code) as DefaultFuelBurnAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultFuelBurnAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultFuelBurnAssignment> = inner.list()
}

fun newCachedDefaultFuelBurnAssignmentStore(inner: DefaultFuelBurnAssignmentStore, ttl: Int, clock: Clock): DefaultFuelBurnAssignmentStore {
    return CachedDefaultFuelBurnAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultFuelBurnAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default fuel burn assignments. Empty fields match all. */
data class DefaultFuelBurnAssignmentQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultFuelBurnAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DefaultFuelBurnAssignment>): MutableList<DefaultFuelBurnAssignment> {
        val matched = filter(items) { candidate: DefaultFuelBurnAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultFuelBurnAssignments(store: DefaultFuelBurnAssignmentStore, query: DefaultFuelBurnAssignmentQuery): MutableList<DefaultFuelBurnAssignment> {
    return query.apply(store.list())
}
