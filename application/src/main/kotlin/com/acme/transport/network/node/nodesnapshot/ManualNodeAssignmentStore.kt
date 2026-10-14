package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual node assignments by code. */
interface ManualNodeAssignmentStore {
    fun find(code: String): ManualNodeAssignment?
    fun save(item: ManualNodeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualNodeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualNodeAssignmentStore(
    var items: MutableMap<String, ManualNodeAssignment>,
    var order: MutableList<String>,
) : ManualNodeAssignmentStore {
    override fun find(code: String): ManualNodeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualNodeAssignment) {
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

    override fun list(): MutableList<ManualNodeAssignment> {
        val out = mutableListOf<ManualNodeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualNodeAssignmentStore(): ManualNodeAssignmentStore {
    return MemoryManualNodeAssignmentStore(
        mutableMapOf<String, ManualNodeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualNodeAssignmentStore(
    var inner: ManualNodeAssignmentStore,
    var cache: TtlCache<ManualNodeAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualNodeAssignmentStore {
    override fun find(code: String): ManualNodeAssignment? {
        val hit = (cache.get(code) as ManualNodeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualNodeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualNodeAssignment> = inner.list()
}

fun newCachedManualNodeAssignmentStore(inner: ManualNodeAssignmentStore, ttl: Int, clock: Clock): ManualNodeAssignmentStore {
    return CachedManualNodeAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualNodeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual node assignments. Empty fields match all. */
data class ManualNodeAssignmentQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualNodeAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualNodeAssignment>): MutableList<ManualNodeAssignment> {
        val matched = filter(items) { candidate: ManualNodeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualNodeAssignments(store: ManualNodeAssignmentStore, query: ManualNodeAssignmentQuery): MutableList<ManualNodeAssignment> {
    return query.apply(store.list())
}
