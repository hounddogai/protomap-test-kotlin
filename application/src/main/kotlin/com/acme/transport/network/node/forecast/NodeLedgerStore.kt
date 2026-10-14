package com.acme.transport.network.node.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node ledgers by code. */
interface NodeLedgerStore {
    fun find(code: String): NodeLedger?
    fun save(item: NodeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeLedgerStore(
    var items: MutableMap<String, NodeLedger>,
    var order: MutableList<String>,
) : NodeLedgerStore {
    override fun find(code: String): NodeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeLedger) {
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

    override fun list(): MutableList<NodeLedger> {
        val out = mutableListOf<NodeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeLedgerStore(): NodeLedgerStore {
    return MemoryNodeLedgerStore(mutableMapOf<String, NodeLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeLedgerStore(
    var inner: NodeLedgerStore,
    var cache: TtlCache<NodeLedger>,
    var hits: Int,
    var misses: Int,
) : NodeLedgerStore {
    override fun find(code: String): NodeLedger? {
        val hit = (cache.get(code) as NodeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeLedger> = inner.list()
}

fun newCachedNodeLedgerStore(inner: NodeLedgerStore, ttl: Int, clock: Clock): NodeLedgerStore {
    return CachedNodeLedgerStore(
        inner = inner,
        cache = newTtlCache<NodeLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node ledgers. Empty fields match all. */
data class NodeLedgerQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeLedger>): MutableList<NodeLedger> {
        val matched = filter(items) { candidate: NodeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeLedgers(store: NodeLedgerStore, query: NodeLedgerQuery): MutableList<NodeLedger> {
    return query.apply(store.list())
}
