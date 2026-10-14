package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel burn assignments by code. */
interface GlobalFuelBurnAssignmentStore {
    fun find(code: String): GlobalFuelBurnAssignment?
    fun save(item: GlobalFuelBurnAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelBurnAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelBurnAssignmentStore(
    var items: MutableMap<String, GlobalFuelBurnAssignment>,
    var order: MutableList<String>,
) : GlobalFuelBurnAssignmentStore {
    override fun find(code: String): GlobalFuelBurnAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelBurnAssignment) {
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

    override fun list(): MutableList<GlobalFuelBurnAssignment> {
        val out = mutableListOf<GlobalFuelBurnAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelBurnAssignmentStore(): GlobalFuelBurnAssignmentStore {
    return MemoryGlobalFuelBurnAssignmentStore(
        mutableMapOf<String, GlobalFuelBurnAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelBurnAssignmentStore(
    var inner: GlobalFuelBurnAssignmentStore,
    var cache: TtlCache<GlobalFuelBurnAssignment>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelBurnAssignmentStore {
    override fun find(code: String): GlobalFuelBurnAssignment? {
        val hit = (cache.get(code) as GlobalFuelBurnAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelBurnAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelBurnAssignment> = inner.list()
}

fun newCachedGlobalFuelBurnAssignmentStore(inner: GlobalFuelBurnAssignmentStore, ttl: Int, clock: Clock): GlobalFuelBurnAssignmentStore {
    return CachedGlobalFuelBurnAssignmentStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelBurnAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel burn assignments. Empty fields match all. */
data class GlobalFuelBurnAssignmentQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelBurnAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelBurnAssignment>): MutableList<GlobalFuelBurnAssignment> {
        val matched = filter(items) { candidate: GlobalFuelBurnAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelBurnAssignments(store: GlobalFuelBurnAssignmentStore, query: GlobalFuelBurnAssignmentQuery): MutableList<GlobalFuelBurnAssignment> {
    return query.apply(store.list())
}
