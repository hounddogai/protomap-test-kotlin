package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door snapshots by code. */
interface CappedDockDoorSnapshotStore {
    fun find(code: String): CappedDockDoorSnapshot?
    fun save(item: CappedDockDoorSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorSnapshotStore(
    var items: MutableMap<String, CappedDockDoorSnapshot>,
    var order: MutableList<String>,
) : CappedDockDoorSnapshotStore {
    override fun find(code: String): CappedDockDoorSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorSnapshot) {
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

    override fun list(): MutableList<CappedDockDoorSnapshot> {
        val out = mutableListOf<CappedDockDoorSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorSnapshotStore(): CappedDockDoorSnapshotStore {
    return MemoryCappedDockDoorSnapshotStore(
        mutableMapOf<String, CappedDockDoorSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorSnapshotStore(
    var inner: CappedDockDoorSnapshotStore,
    var cache: TtlCache<CappedDockDoorSnapshot>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorSnapshotStore {
    override fun find(code: String): CappedDockDoorSnapshot? {
        val hit = (cache.get(code) as CappedDockDoorSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorSnapshot> = inner.list()
}

fun newCachedCappedDockDoorSnapshotStore(inner: CappedDockDoorSnapshotStore, ttl: Int, clock: Clock): CappedDockDoorSnapshotStore {
    return CachedCappedDockDoorSnapshotStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door snapshots. Empty fields match all. */
data class CappedDockDoorSnapshotQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDockDoorSnapshot>): MutableList<CappedDockDoorSnapshot> {
        val matched = filter(items) { candidate: CappedDockDoorSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorSnapshots(store: CappedDockDoorSnapshotStore, query: CappedDockDoorSnapshotQuery): MutableList<CappedDockDoorSnapshot> {
    return query.apply(store.list())
}
