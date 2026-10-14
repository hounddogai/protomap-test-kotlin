package com.acme.transport.network.node.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node policies by code. */
interface NodePolicyStore {
    fun find(code: String): NodePolicy?
    fun save(item: NodePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodePolicyStore(
    var items: MutableMap<String, NodePolicy>,
    var order: MutableList<String>,
) : NodePolicyStore {
    override fun find(code: String): NodePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodePolicy) {
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

    override fun list(): MutableList<NodePolicy> {
        val out = mutableListOf<NodePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodePolicyStore(): NodePolicyStore {
    return MemoryNodePolicyStore(mutableMapOf<String, NodePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodePolicyStore(
    var inner: NodePolicyStore,
    var cache: TtlCache<NodePolicy>,
    var hits: Int,
    var misses: Int,
) : NodePolicyStore {
    override fun find(code: String): NodePolicy? {
        val hit = (cache.get(code) as NodePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodePolicy> = inner.list()
}

fun newCachedNodePolicyStore(inner: NodePolicyStore, ttl: Int, clock: Clock): NodePolicyStore {
    return CachedNodePolicyStore(
        inner = inner,
        cache = newTtlCache<NodePolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node policies. Empty fields match all. */
data class NodePolicyQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodePolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodePolicy>): MutableList<NodePolicy> {
        val matched = filter(items) { candidate: NodePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodePolicies(store: NodePolicyStore, query: NodePolicyQuery): MutableList<NodePolicy> {
    return query.apply(store.list())
}
