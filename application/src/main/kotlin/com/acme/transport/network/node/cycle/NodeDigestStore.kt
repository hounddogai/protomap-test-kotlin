package com.acme.transport.network.node.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node digests by code. */
interface NodeDigestStore {
    fun find(code: String): NodeDigest?
    fun save(item: NodeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeDigestStore(
    var items: MutableMap<String, NodeDigest>,
    var order: MutableList<String>,
) : NodeDigestStore {
    override fun find(code: String): NodeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeDigest) {
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

    override fun list(): MutableList<NodeDigest> {
        val out = mutableListOf<NodeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeDigestStore(): NodeDigestStore {
    return MemoryNodeDigestStore(mutableMapOf<String, NodeDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeDigestStore(
    var inner: NodeDigestStore,
    var cache: TtlCache<NodeDigest>,
    var hits: Int,
    var misses: Int,
) : NodeDigestStore {
    override fun find(code: String): NodeDigest? {
        val hit = (cache.get(code) as NodeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeDigest> = inner.list()
}

fun newCachedNodeDigestStore(inner: NodeDigestStore, ttl: Int, clock: Clock): NodeDigestStore {
    return CachedNodeDigestStore(
        inner = inner,
        cache = newTtlCache<NodeDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node digests. Empty fields match all. */
data class NodeDigestQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeDigest>): MutableList<NodeDigest> {
        val matched = filter(items) { candidate: NodeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeDigests(store: NodeDigestStore, query: NodeDigestQuery): MutableList<NodeDigest> {
    return query.apply(store.list())
}
