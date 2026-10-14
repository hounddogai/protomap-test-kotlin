package com.acme.transport.network.node.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node agreements by code. */
interface NodeAgreementStore {
    fun find(code: String): NodeAgreement?
    fun save(item: NodeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeAgreementStore(
    var items: MutableMap<String, NodeAgreement>,
    var order: MutableList<String>,
) : NodeAgreementStore {
    override fun find(code: String): NodeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeAgreement) {
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

    override fun list(): MutableList<NodeAgreement> {
        val out = mutableListOf<NodeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeAgreementStore(): NodeAgreementStore {
    return MemoryNodeAgreementStore(mutableMapOf<String, NodeAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeAgreementStore(
    var inner: NodeAgreementStore,
    var cache: TtlCache<NodeAgreement>,
    var hits: Int,
    var misses: Int,
) : NodeAgreementStore {
    override fun find(code: String): NodeAgreement? {
        val hit = (cache.get(code) as NodeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeAgreement> = inner.list()
}

fun newCachedNodeAgreementStore(inner: NodeAgreementStore, ttl: Int, clock: Clock): NodeAgreementStore {
    return CachedNodeAgreementStore(
        inner = inner,
        cache = newTtlCache<NodeAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node agreements. Empty fields match all. */
data class NodeAgreementQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeAgreement>): MutableList<NodeAgreement> {
        val matched = filter(items) { candidate: NodeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeAgreements(store: NodeAgreementStore, query: NodeAgreementQuery): MutableList<NodeAgreement> {
    return query.apply(store.list())
}
