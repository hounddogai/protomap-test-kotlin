package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node records by code. */
interface NodeRecordStore {
    fun find(code: String): NodeRecord?
    fun save(item: NodeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeRecordStore(
    var items: MutableMap<String, NodeRecord>,
    var order: MutableList<String>,
) : NodeRecordStore {
    override fun find(code: String): NodeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeRecord) {
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

    override fun list(): MutableList<NodeRecord> {
        val out = mutableListOf<NodeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeRecordStore(): NodeRecordStore {
    return MemoryNodeRecordStore(mutableMapOf<String, NodeRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeRecordStore(
    var inner: NodeRecordStore,
    var cache: TtlCache<NodeRecord>,
    var hits: Int,
    var misses: Int,
) : NodeRecordStore {
    override fun find(code: String): NodeRecord? {
        val hit = (cache.get(code) as NodeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeRecord> = inner.list()
}

fun newCachedNodeRecordStore(inner: NodeRecordStore, ttl: Int, clock: Clock): NodeRecordStore {
    return CachedNodeRecordStore(
        inner = inner,
        cache = newTtlCache<NodeRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node records. Empty fields match all. */
data class NodeRecordQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeRecord>): MutableList<NodeRecord> {
        val matched = filter(items) { candidate: NodeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeRecords(store: NodeRecordStore, query: NodeRecordQuery): MutableList<NodeRecord> {
    return query.apply(store.list())
}
