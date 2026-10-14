package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending swap snapshots by code. */
interface PendingSwapSnapshotStore {
    fun find(code: String): PendingSwapSnapshot?
    fun save(item: PendingSwapSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSwapSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSwapSnapshotStore(
    var items: MutableMap<String, PendingSwapSnapshot>,
    var order: MutableList<String>,
) : PendingSwapSnapshotStore {
    override fun find(code: String): PendingSwapSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSwapSnapshot) {
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

    override fun list(): MutableList<PendingSwapSnapshot> {
        val out = mutableListOf<PendingSwapSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSwapSnapshotStore(): PendingSwapSnapshotStore {
    return MemoryPendingSwapSnapshotStore(mutableMapOf<String, PendingSwapSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSwapSnapshotStore(
    var inner: PendingSwapSnapshotStore,
    var cache: TtlCache<PendingSwapSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingSwapSnapshotStore {
    override fun find(code: String): PendingSwapSnapshot? {
        val hit = (cache.get(code) as PendingSwapSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSwapSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSwapSnapshot> = inner.list()
}

fun newCachedPendingSwapSnapshotStore(inner: PendingSwapSnapshotStore, ttl: Int, clock: Clock): PendingSwapSnapshotStore {
    return CachedPendingSwapSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingSwapSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending swap snapshots. Empty fields match all. */
data class PendingSwapSnapshotQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSwapSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSwapSnapshot>): MutableList<PendingSwapSnapshot> {
        val matched = filter(items) { candidate: PendingSwapSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSwapSnapshots(store: PendingSwapSnapshotStore, query: PendingSwapSnapshotQuery): MutableList<PendingSwapSnapshot> {
    return query.apply(store.list())
}
