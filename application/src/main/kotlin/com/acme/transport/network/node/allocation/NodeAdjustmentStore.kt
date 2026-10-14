package com.acme.transport.network.node.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node adjustments by code. */
interface NodeAdjustmentStore {
    fun find(code: String): NodeAdjustment?
    fun save(item: NodeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeAdjustmentStore(
    var items: MutableMap<String, NodeAdjustment>,
    var order: MutableList<String>,
) : NodeAdjustmentStore {
    override fun find(code: String): NodeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeAdjustment) {
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

    override fun list(): MutableList<NodeAdjustment> {
        val out = mutableListOf<NodeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeAdjustmentStore(): NodeAdjustmentStore {
    return MemoryNodeAdjustmentStore(mutableMapOf<String, NodeAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeAdjustmentStore(
    var inner: NodeAdjustmentStore,
    var cache: TtlCache<NodeAdjustment>,
    var hits: Int,
    var misses: Int,
) : NodeAdjustmentStore {
    override fun find(code: String): NodeAdjustment? {
        val hit = (cache.get(code) as NodeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeAdjustment> = inner.list()
}

fun newCachedNodeAdjustmentStore(inner: NodeAdjustmentStore, ttl: Int, clock: Clock): NodeAdjustmentStore {
    return CachedNodeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<NodeAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node adjustments. Empty fields match all. */
data class NodeAdjustmentQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeAdjustment>): MutableList<NodeAdjustment> {
        val matched = filter(items) { candidate: NodeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeAdjustments(store: NodeAdjustmentStore, query: NodeAdjustmentQuery): MutableList<NodeAdjustment> {
    return query.apply(store.list())
}
