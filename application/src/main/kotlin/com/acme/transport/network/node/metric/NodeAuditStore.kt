package com.acme.transport.network.node.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node audits by code. */
interface NodeAuditStore {
    fun find(code: String): NodeAudit?
    fun save(item: NodeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeAuditStore(
    var items: MutableMap<String, NodeAudit>,
    var order: MutableList<String>,
) : NodeAuditStore {
    override fun find(code: String): NodeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeAudit) {
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

    override fun list(): MutableList<NodeAudit> {
        val out = mutableListOf<NodeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeAuditStore(): NodeAuditStore {
    return MemoryNodeAuditStore(mutableMapOf<String, NodeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeAuditStore(
    var inner: NodeAuditStore,
    var cache: TtlCache<NodeAudit>,
    var hits: Int,
    var misses: Int,
) : NodeAuditStore {
    override fun find(code: String): NodeAudit? {
        val hit = (cache.get(code) as NodeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeAudit> = inner.list()
}

fun newCachedNodeAuditStore(inner: NodeAuditStore, ttl: Int, clock: Clock): NodeAuditStore {
    return CachedNodeAuditStore(
        inner = inner,
        cache = newTtlCache<NodeAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node audits. Empty fields match all. */
data class NodeAuditQuery(
    var segment: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeAudit>): MutableList<NodeAudit> {
        val matched = filter(items) { candidate: NodeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeAudits(store: NodeAuditStore, query: NodeAuditQuery): MutableList<NodeAudit> {
    return query.apply(store.list())
}
