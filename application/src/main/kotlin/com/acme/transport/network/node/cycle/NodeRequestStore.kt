package com.acme.transport.network.node.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node requests by code. */
interface NodeRequestStore {
    fun find(code: String): NodeRequest?
    fun save(item: NodeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeRequestStore(
    var items: MutableMap<String, NodeRequest>,
    var order: MutableList<String>,
) : NodeRequestStore {
    override fun find(code: String): NodeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeRequest) {
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

    override fun list(): MutableList<NodeRequest> {
        val out = mutableListOf<NodeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeRequestStore(): NodeRequestStore {
    return MemoryNodeRequestStore(mutableMapOf<String, NodeRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeRequestStore(
    var inner: NodeRequestStore,
    var cache: TtlCache<NodeRequest>,
    var hits: Int,
    var misses: Int,
) : NodeRequestStore {
    override fun find(code: String): NodeRequest? {
        val hit = (cache.get(code) as NodeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeRequest> = inner.list()
}

fun newCachedNodeRequestStore(inner: NodeRequestStore, ttl: Int, clock: Clock): NodeRequestStore {
    return CachedNodeRequestStore(
        inner = inner,
        cache = newTtlCache<NodeRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node requests. Empty fields match all. */
data class NodeRequestQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeRequest>): MutableList<NodeRequest> {
        val matched = filter(items) { candidate: NodeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeRequests(store: NodeRequestStore, query: NodeRequestQuery): MutableList<NodeRequest> {
    return query.apply(store.list())
}
