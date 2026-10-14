package com.acme.transport.network.node.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node snapshots by code. */
interface NodeSnapshotStore {
    fun find(code: String): NodeSnapshot?
    fun save(item: NodeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeSnapshotStore(
    var items: MutableMap<String, NodeSnapshot>,
    var order: MutableList<String>,
) : NodeSnapshotStore {
    override fun find(code: String): NodeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeSnapshot) {
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

    override fun list(): MutableList<NodeSnapshot> {
        val out = mutableListOf<NodeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeSnapshotStore(): NodeSnapshotStore {
    return MemoryNodeSnapshotStore(mutableMapOf<String, NodeSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeSnapshotStore(
    var inner: NodeSnapshotStore,
    var cache: TtlCache<NodeSnapshot>,
    var hits: Int,
    var misses: Int,
) : NodeSnapshotStore {
    override fun find(code: String): NodeSnapshot? {
        val hit = (cache.get(code) as NodeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeSnapshot> = inner.list()
}

fun newCachedNodeSnapshotStore(inner: NodeSnapshotStore, ttl: Int, clock: Clock): NodeSnapshotStore {
    return CachedNodeSnapshotStore(
        inner = inner,
        cache = newTtlCache<NodeSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node snapshots. Empty fields match all. */
data class NodeSnapshotQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeSnapshot>): MutableList<NodeSnapshot> {
        val matched = filter(items) { candidate: NodeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeSnapshots(store: NodeSnapshotStore, query: NodeSnapshotQuery): MutableList<NodeSnapshot> {
    return query.apply(store.list())
}
