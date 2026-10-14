package com.acme.transport.network.node.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node checkpoints by code. */
interface NodeCheckpointStore {
    fun find(code: String): NodeCheckpoint?
    fun save(item: NodeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeCheckpointStore(
    var items: MutableMap<String, NodeCheckpoint>,
    var order: MutableList<String>,
) : NodeCheckpointStore {
    override fun find(code: String): NodeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeCheckpoint) {
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

    override fun list(): MutableList<NodeCheckpoint> {
        val out = mutableListOf<NodeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeCheckpointStore(): NodeCheckpointStore {
    return MemoryNodeCheckpointStore(mutableMapOf<String, NodeCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeCheckpointStore(
    var inner: NodeCheckpointStore,
    var cache: TtlCache<NodeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : NodeCheckpointStore {
    override fun find(code: String): NodeCheckpoint? {
        val hit = (cache.get(code) as NodeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeCheckpoint> = inner.list()
}

fun newCachedNodeCheckpointStore(inner: NodeCheckpointStore, ttl: Int, clock: Clock): NodeCheckpointStore {
    return CachedNodeCheckpointStore(
        inner = inner,
        cache = newTtlCache<NodeCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node checkpoints. Empty fields match all. */
data class NodeCheckpointQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeCheckpoint>): MutableList<NodeCheckpoint> {
        val matched = filter(items) { candidate: NodeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeCheckpoints(store: NodeCheckpointStore, query: NodeCheckpointQuery): MutableList<NodeCheckpoint> {
    return query.apply(store.list())
}
