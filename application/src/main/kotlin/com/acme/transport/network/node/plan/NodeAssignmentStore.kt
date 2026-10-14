package com.acme.transport.network.node.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node assignments by code. */
interface NodeAssignmentStore {
    fun find(code: String): NodeAssignment?
    fun save(item: NodeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeAssignmentStore(
    var items: MutableMap<String, NodeAssignment>,
    var order: MutableList<String>,
) : NodeAssignmentStore {
    override fun find(code: String): NodeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeAssignment) {
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

    override fun list(): MutableList<NodeAssignment> {
        val out = mutableListOf<NodeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeAssignmentStore(): NodeAssignmentStore {
    return MemoryNodeAssignmentStore(mutableMapOf<String, NodeAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeAssignmentStore(
    var inner: NodeAssignmentStore,
    var cache: TtlCache<NodeAssignment>,
    var hits: Int,
    var misses: Int,
) : NodeAssignmentStore {
    override fun find(code: String): NodeAssignment? {
        val hit = (cache.get(code) as NodeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeAssignment> = inner.list()
}

fun newCachedNodeAssignmentStore(inner: NodeAssignmentStore, ttl: Int, clock: Clock): NodeAssignmentStore {
    return CachedNodeAssignmentStore(
        inner = inner,
        cache = newTtlCache<NodeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node assignments. Empty fields match all. */
data class NodeAssignmentQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeAssignment>): MutableList<NodeAssignment> {
        val matched = filter(items) { candidate: NodeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeAssignments(store: NodeAssignmentStore, query: NodeAssignmentQuery): MutableList<NodeAssignment> {
    return query.apply(store.list())
}
