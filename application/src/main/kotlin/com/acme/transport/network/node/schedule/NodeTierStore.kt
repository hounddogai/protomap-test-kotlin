package com.acme.transport.network.node.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node tiers by code. */
interface NodeTierStore {
    fun find(code: String): NodeTier?
    fun save(item: NodeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeTierStore(
    var items: MutableMap<String, NodeTier>,
    var order: MutableList<String>,
) : NodeTierStore {
    override fun find(code: String): NodeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeTier) {
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

    override fun list(): MutableList<NodeTier> {
        val out = mutableListOf<NodeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeTierStore(): NodeTierStore {
    return MemoryNodeTierStore(mutableMapOf<String, NodeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeTierStore(
    var inner: NodeTierStore,
    var cache: TtlCache<NodeTier>,
    var hits: Int,
    var misses: Int,
) : NodeTierStore {
    override fun find(code: String): NodeTier? {
        val hit = (cache.get(code) as NodeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeTier> = inner.list()
}

fun newCachedNodeTierStore(inner: NodeTierStore, ttl: Int, clock: Clock): NodeTierStore {
    return CachedNodeTierStore(
        inner = inner,
        cache = newTtlCache<NodeTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node tiers. Empty fields match all. */
data class NodeTierQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeTier>): MutableList<NodeTier> {
        val matched = filter(items) { candidate: NodeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeTiers(store: NodeTierStore, query: NodeTierQuery): MutableList<NodeTier> = query.apply(store.list())
