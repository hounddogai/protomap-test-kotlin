package com.acme.transport.network.node.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node mappings by code. */
interface NodeMappingStore {
    fun find(code: String): NodeMapping?
    fun save(item: NodeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeMappingStore(
    var items: MutableMap<String, NodeMapping>,
    var order: MutableList<String>,
) : NodeMappingStore {
    override fun find(code: String): NodeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeMapping) {
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

    override fun list(): MutableList<NodeMapping> {
        val out = mutableListOf<NodeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeMappingStore(): NodeMappingStore {
    return MemoryNodeMappingStore(mutableMapOf<String, NodeMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeMappingStore(
    var inner: NodeMappingStore,
    var cache: TtlCache<NodeMapping>,
    var hits: Int,
    var misses: Int,
) : NodeMappingStore {
    override fun find(code: String): NodeMapping? {
        val hit = (cache.get(code) as NodeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeMapping> = inner.list()
}

fun newCachedNodeMappingStore(inner: NodeMappingStore, ttl: Int, clock: Clock): NodeMappingStore {
    return CachedNodeMappingStore(
        inner = inner,
        cache = newTtlCache<NodeMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node mappings. Empty fields match all. */
data class NodeMappingQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeMapping>): MutableList<NodeMapping> {
        val matched = filter(items) { candidate: NodeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeMappings(store: NodeMappingStore, query: NodeMappingQuery): MutableList<NodeMapping> {
    return query.apply(store.list())
}
