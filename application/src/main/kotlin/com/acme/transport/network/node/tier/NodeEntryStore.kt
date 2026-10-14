package com.acme.transport.network.node.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node entries by code. */
interface NodeEntryStore {
    fun find(code: String): NodeEntry?
    fun save(item: NodeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeEntryStore(
    var items: MutableMap<String, NodeEntry>,
    var order: MutableList<String>,
) : NodeEntryStore {
    override fun find(code: String): NodeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeEntry) {
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

    override fun list(): MutableList<NodeEntry> {
        val out = mutableListOf<NodeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeEntryStore(): NodeEntryStore {
    return MemoryNodeEntryStore(mutableMapOf<String, NodeEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeEntryStore(
    var inner: NodeEntryStore,
    var cache: TtlCache<NodeEntry>,
    var hits: Int,
    var misses: Int,
) : NodeEntryStore {
    override fun find(code: String): NodeEntry? {
        val hit = (cache.get(code) as NodeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeEntry> = inner.list()
}

fun newCachedNodeEntryStore(inner: NodeEntryStore, ttl: Int, clock: Clock): NodeEntryStore {
    return CachedNodeEntryStore(
        inner = inner,
        cache = newTtlCache<NodeEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node entries. Empty fields match all. */
data class NodeEntryQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeEntry>): MutableList<NodeEntry> {
        val matched = filter(items) { candidate: NodeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeEntries(store: NodeEntryStore, query: NodeEntryQuery): MutableList<NodeEntry> {
    return query.apply(store.list())
}
