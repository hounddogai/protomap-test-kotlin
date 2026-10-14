package com.acme.transport.network.node.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node milestones by code. */
interface NodeMilestoneStore {
    fun find(code: String): NodeMilestone?
    fun save(item: NodeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeMilestoneStore(
    var items: MutableMap<String, NodeMilestone>,
    var order: MutableList<String>,
) : NodeMilestoneStore {
    override fun find(code: String): NodeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeMilestone) {
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

    override fun list(): MutableList<NodeMilestone> {
        val out = mutableListOf<NodeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeMilestoneStore(): NodeMilestoneStore {
    return MemoryNodeMilestoneStore(mutableMapOf<String, NodeMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeMilestoneStore(
    var inner: NodeMilestoneStore,
    var cache: TtlCache<NodeMilestone>,
    var hits: Int,
    var misses: Int,
) : NodeMilestoneStore {
    override fun find(code: String): NodeMilestone? {
        val hit = (cache.get(code) as NodeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeMilestone> = inner.list()
}

fun newCachedNodeMilestoneStore(inner: NodeMilestoneStore, ttl: Int, clock: Clock): NodeMilestoneStore {
    return CachedNodeMilestoneStore(
        inner = inner,
        cache = newTtlCache<NodeMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node milestones. Empty fields match all. */
data class NodeMilestoneQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeMilestone>): MutableList<NodeMilestone> {
        val matched = filter(items) { candidate: NodeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeMilestones(store: NodeMilestoneStore, query: NodeMilestoneQuery): MutableList<NodeMilestone> {
    return query.apply(store.list())
}
