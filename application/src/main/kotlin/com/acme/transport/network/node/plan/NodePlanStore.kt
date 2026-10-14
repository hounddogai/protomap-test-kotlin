package com.acme.transport.network.node.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node plans by code. */
interface NodePlanStore {
    fun find(code: String): NodePlan?
    fun save(item: NodePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodePlanStore(
    var items: MutableMap<String, NodePlan>,
    var order: MutableList<String>,
) : NodePlanStore {
    override fun find(code: String): NodePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodePlan) {
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

    override fun list(): MutableList<NodePlan> {
        val out = mutableListOf<NodePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodePlanStore(): NodePlanStore {
    return MemoryNodePlanStore(mutableMapOf<String, NodePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodePlanStore(
    var inner: NodePlanStore,
    var cache: TtlCache<NodePlan>,
    var hits: Int,
    var misses: Int,
) : NodePlanStore {
    override fun find(code: String): NodePlan? {
        val hit = (cache.get(code) as NodePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodePlan> = inner.list()
}

fun newCachedNodePlanStore(inner: NodePlanStore, ttl: Int, clock: Clock): NodePlanStore {
    return CachedNodePlanStore(
        inner = inner,
        cache = newTtlCache<NodePlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node plans. Empty fields match all. */
data class NodePlanQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodePlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<NodePlan>): MutableList<NodePlan> {
        val matched = filter(items) { candidate: NodePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodePlans(store: NodePlanStore, query: NodePlanQuery): MutableList<NodePlan> = query.apply(store.list())
