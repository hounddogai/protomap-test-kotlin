package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional node snapshots by code. */
interface RegionalNodeSnapshotStore {
    fun find(code: String): RegionalNodeSnapshot?
    fun save(item: RegionalNodeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalNodeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalNodeSnapshotStore(
    var items: MutableMap<String, RegionalNodeSnapshot>,
    var order: MutableList<String>,
) : RegionalNodeSnapshotStore {
    override fun find(code: String): RegionalNodeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalNodeSnapshot) {
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

    override fun list(): MutableList<RegionalNodeSnapshot> {
        val out = mutableListOf<RegionalNodeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalNodeSnapshotStore(): RegionalNodeSnapshotStore {
    return MemoryRegionalNodeSnapshotStore(
        mutableMapOf<String, RegionalNodeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalNodeSnapshotStore(
    var inner: RegionalNodeSnapshotStore,
    var cache: TtlCache<RegionalNodeSnapshot>,
    var hits: Int,
    var misses: Int,
) : RegionalNodeSnapshotStore {
    override fun find(code: String): RegionalNodeSnapshot? {
        val hit = (cache.get(code) as RegionalNodeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalNodeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalNodeSnapshot> = inner.list()
}

fun newCachedRegionalNodeSnapshotStore(inner: RegionalNodeSnapshotStore, ttl: Int, clock: Clock): RegionalNodeSnapshotStore {
    return CachedRegionalNodeSnapshotStore(
        inner = inner,
        cache = newTtlCache<RegionalNodeSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional node snapshots. Empty fields match all. */
data class RegionalNodeSnapshotQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalNodeSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalNodeSnapshot>): MutableList<RegionalNodeSnapshot> {
        val matched = filter(items) { candidate: RegionalNodeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalNodeSnapshots(store: RegionalNodeSnapshotStore, query: RegionalNodeSnapshotQuery): MutableList<RegionalNodeSnapshot> {
    return query.apply(store.list())
}
