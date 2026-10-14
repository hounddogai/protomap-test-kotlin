package com.acme.transport.network.node.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node segments by code. */
interface NodeSegmentStore {
    fun find(code: String): NodeSegment?
    fun save(item: NodeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeSegmentStore(
    var items: MutableMap<String, NodeSegment>,
    var order: MutableList<String>,
) : NodeSegmentStore {
    override fun find(code: String): NodeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeSegment) {
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

    override fun list(): MutableList<NodeSegment> {
        val out = mutableListOf<NodeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeSegmentStore(): NodeSegmentStore {
    return MemoryNodeSegmentStore(mutableMapOf<String, NodeSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeSegmentStore(
    var inner: NodeSegmentStore,
    var cache: TtlCache<NodeSegment>,
    var hits: Int,
    var misses: Int,
) : NodeSegmentStore {
    override fun find(code: String): NodeSegment? {
        val hit = (cache.get(code) as NodeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeSegment> = inner.list()
}

fun newCachedNodeSegmentStore(inner: NodeSegmentStore, ttl: Int, clock: Clock): NodeSegmentStore {
    return CachedNodeSegmentStore(
        inner = inner,
        cache = newTtlCache<NodeSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node segments. Empty fields match all. */
data class NodeSegmentQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeSegment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeSegment>): MutableList<NodeSegment> {
        val matched = filter(items) { candidate: NodeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeSegments(store: NodeSegmentStore, query: NodeSegmentQuery): MutableList<NodeSegment> {
    return query.apply(store.list())
}
