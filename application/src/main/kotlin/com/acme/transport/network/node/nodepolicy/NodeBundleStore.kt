package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node bundles by code. */
interface NodeBundleStore {
    fun find(code: String): NodeBundle?
    fun save(item: NodeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeBundleStore(
    var items: MutableMap<String, NodeBundle>,
    var order: MutableList<String>,
) : NodeBundleStore {
    override fun find(code: String): NodeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeBundle) {
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

    override fun list(): MutableList<NodeBundle> {
        val out = mutableListOf<NodeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeBundleStore(): NodeBundleStore {
    return MemoryNodeBundleStore(mutableMapOf<String, NodeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeBundleStore(
    var inner: NodeBundleStore,
    var cache: TtlCache<NodeBundle>,
    var hits: Int,
    var misses: Int,
) : NodeBundleStore {
    override fun find(code: String): NodeBundle? {
        val hit = (cache.get(code) as NodeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeBundle> = inner.list()
}

fun newCachedNodeBundleStore(inner: NodeBundleStore, ttl: Int, clock: Clock): NodeBundleStore {
    return CachedNodeBundleStore(
        inner = inner,
        cache = newTtlCache<NodeBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node bundles. Empty fields match all. */
data class NodeBundleQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeBundle>): MutableList<NodeBundle> {
        val matched = filter(items) { candidate: NodeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeBundles(store: NodeBundleStore, query: NodeBundleQuery): MutableList<NodeBundle> {
    return query.apply(store.list())
}
