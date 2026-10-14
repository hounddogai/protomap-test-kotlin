package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node templates by code. */
interface NodeTemplateStore {
    fun find(code: String): NodeTemplate?
    fun save(item: NodeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeTemplateStore(
    var items: MutableMap<String, NodeTemplate>,
    var order: MutableList<String>,
) : NodeTemplateStore {
    override fun find(code: String): NodeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeTemplate) {
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

    override fun list(): MutableList<NodeTemplate> {
        val out = mutableListOf<NodeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeTemplateStore(): NodeTemplateStore {
    return MemoryNodeTemplateStore(mutableMapOf<String, NodeTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeTemplateStore(
    var inner: NodeTemplateStore,
    var cache: TtlCache<NodeTemplate>,
    var hits: Int,
    var misses: Int,
) : NodeTemplateStore {
    override fun find(code: String): NodeTemplate? {
        val hit = (cache.get(code) as NodeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeTemplate> = inner.list()
}

fun newCachedNodeTemplateStore(inner: NodeTemplateStore, ttl: Int, clock: Clock): NodeTemplateStore {
    return CachedNodeTemplateStore(
        inner = inner,
        cache = newTtlCache<NodeTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node templates. Empty fields match all. */
data class NodeTemplateQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeTemplate): Boolean {
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

    fun apply(items: MutableList<NodeTemplate>): MutableList<NodeTemplate> {
        val matched = filter(items) { candidate: NodeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeTemplates(store: NodeTemplateStore, query: NodeTemplateQuery): MutableList<NodeTemplate> {
    return query.apply(store.list())
}
