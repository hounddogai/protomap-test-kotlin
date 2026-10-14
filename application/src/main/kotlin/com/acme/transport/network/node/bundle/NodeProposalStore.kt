package com.acme.transport.network.node.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node proposals by code. */
interface NodeProposalStore {
    fun find(code: String): NodeProposal?
    fun save(item: NodeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeProposalStore(
    var items: MutableMap<String, NodeProposal>,
    var order: MutableList<String>,
) : NodeProposalStore {
    override fun find(code: String): NodeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeProposal) {
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

    override fun list(): MutableList<NodeProposal> {
        val out = mutableListOf<NodeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeProposalStore(): NodeProposalStore {
    return MemoryNodeProposalStore(mutableMapOf<String, NodeProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeProposalStore(
    var inner: NodeProposalStore,
    var cache: TtlCache<NodeProposal>,
    var hits: Int,
    var misses: Int,
) : NodeProposalStore {
    override fun find(code: String): NodeProposal? {
        val hit = (cache.get(code) as NodeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeProposal> = inner.list()
}

fun newCachedNodeProposalStore(inner: NodeProposalStore, ttl: Int, clock: Clock): NodeProposalStore {
    return CachedNodeProposalStore(
        inner = inner,
        cache = newTtlCache<NodeProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node proposals. Empty fields match all. */
data class NodeProposalQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeProposal): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeProposal>): MutableList<NodeProposal> {
        val matched = filter(items) { candidate: NodeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeProposals(store: NodeProposalStore, query: NodeProposalQuery): MutableList<NodeProposal> {
    return query.apply(store.list())
}
