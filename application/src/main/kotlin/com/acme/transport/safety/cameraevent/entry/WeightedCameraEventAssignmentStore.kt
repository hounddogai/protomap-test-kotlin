package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted camera event assignments by code. */
interface WeightedCameraEventAssignmentStore {
    fun find(code: String): WeightedCameraEventAssignment?
    fun save(item: WeightedCameraEventAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCameraEventAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCameraEventAssignmentStore(
    var items: MutableMap<String, WeightedCameraEventAssignment>,
    var order: MutableList<String>,
) : WeightedCameraEventAssignmentStore {
    override fun find(code: String): WeightedCameraEventAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCameraEventAssignment) {
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

    override fun list(): MutableList<WeightedCameraEventAssignment> {
        val out = mutableListOf<WeightedCameraEventAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCameraEventAssignmentStore(): WeightedCameraEventAssignmentStore {
    return MemoryWeightedCameraEventAssignmentStore(
        mutableMapOf<String, WeightedCameraEventAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCameraEventAssignmentStore(
    var inner: WeightedCameraEventAssignmentStore,
    var cache: TtlCache<WeightedCameraEventAssignment>,
    var hits: Int,
    var misses: Int,
) : WeightedCameraEventAssignmentStore {
    override fun find(code: String): WeightedCameraEventAssignment? {
        val hit = (cache.get(code) as WeightedCameraEventAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCameraEventAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCameraEventAssignment> = inner.list()
}

fun newCachedWeightedCameraEventAssignmentStore(inner: WeightedCameraEventAssignmentStore, ttl: Int, clock: Clock): WeightedCameraEventAssignmentStore {
    return CachedWeightedCameraEventAssignmentStore(
        inner = inner,
        cache = newTtlCache<WeightedCameraEventAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted camera event assignments. Empty fields match all. */
data class WeightedCameraEventAssignmentQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCameraEventAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCameraEventAssignment>): MutableList<WeightedCameraEventAssignment> {
        val matched = filter(items) { candidate: WeightedCameraEventAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCameraEventAssignments(store: WeightedCameraEventAssignmentStore, query: WeightedCameraEventAssignmentQuery): MutableList<WeightedCameraEventAssignment> {
    return query.apply(store.list())
}
