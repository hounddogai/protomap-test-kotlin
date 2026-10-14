package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending cross dock snapshots by code. */
interface PendingCrossDockSnapshotStore {
    fun find(code: String): PendingCrossDockSnapshot?
    fun save(item: PendingCrossDockSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCrossDockSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCrossDockSnapshotStore(
    var items: MutableMap<String, PendingCrossDockSnapshot>,
    var order: MutableList<String>,
) : PendingCrossDockSnapshotStore {
    override fun find(code: String): PendingCrossDockSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCrossDockSnapshot) {
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

    override fun list(): MutableList<PendingCrossDockSnapshot> {
        val out = mutableListOf<PendingCrossDockSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCrossDockSnapshotStore(): PendingCrossDockSnapshotStore {
    return MemoryPendingCrossDockSnapshotStore(
        mutableMapOf<String, PendingCrossDockSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCrossDockSnapshotStore(
    var inner: PendingCrossDockSnapshotStore,
    var cache: TtlCache<PendingCrossDockSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingCrossDockSnapshotStore {
    override fun find(code: String): PendingCrossDockSnapshot? {
        val hit = (cache.get(code) as PendingCrossDockSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCrossDockSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCrossDockSnapshot> = inner.list()
}

fun newCachedPendingCrossDockSnapshotStore(inner: PendingCrossDockSnapshotStore, ttl: Int, clock: Clock): PendingCrossDockSnapshotStore {
    return CachedPendingCrossDockSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingCrossDockSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cross dock snapshots. Empty fields match all. */
data class PendingCrossDockSnapshotQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCrossDockSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<PendingCrossDockSnapshot>): MutableList<PendingCrossDockSnapshot> {
        val matched = filter(items) { candidate: PendingCrossDockSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCrossDockSnapshots(store: PendingCrossDockSnapshotStore, query: PendingCrossDockSnapshotQuery): MutableList<PendingCrossDockSnapshot> {
    return query.apply(store.list())
}
