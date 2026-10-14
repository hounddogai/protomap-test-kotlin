package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door snapshots by code. */
interface DockDoorSnapshotStore {
    fun find(code: String): DockDoorSnapshot?
    fun save(item: DockDoorSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorSnapshotStore(
    var items: MutableMap<String, DockDoorSnapshot>,
    var order: MutableList<String>,
) : DockDoorSnapshotStore {
    override fun find(code: String): DockDoorSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorSnapshot) {
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

    override fun list(): MutableList<DockDoorSnapshot> {
        val out = mutableListOf<DockDoorSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorSnapshotStore(): DockDoorSnapshotStore {
    return MemoryDockDoorSnapshotStore(mutableMapOf<String, DockDoorSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorSnapshotStore(
    var inner: DockDoorSnapshotStore,
    var cache: TtlCache<DockDoorSnapshot>,
    var hits: Int,
    var misses: Int,
) : DockDoorSnapshotStore {
    override fun find(code: String): DockDoorSnapshot? {
        val hit = (cache.get(code) as DockDoorSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorSnapshot> = inner.list()
}

fun newCachedDockDoorSnapshotStore(inner: DockDoorSnapshotStore, ttl: Int, clock: Clock): DockDoorSnapshotStore {
    return CachedDockDoorSnapshotStore(
        inner = inner,
        cache = newTtlCache<DockDoorSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door snapshots. Empty fields match all. */
data class DockDoorSnapshotQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorSnapshot>): MutableList<DockDoorSnapshot> {
        val matched = filter(items) { candidate: DockDoorSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorSnapshots(store: DockDoorSnapshotStore, query: DockDoorSnapshotQuery): MutableList<DockDoorSnapshot> {
    return query.apply(store.list())
}
