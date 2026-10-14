package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending ev charge snapshots by code. */
interface PendingEvChargeSnapshotStore {
    fun find(code: String): PendingEvChargeSnapshot?
    fun save(item: PendingEvChargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEvChargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEvChargeSnapshotStore(
    var items: MutableMap<String, PendingEvChargeSnapshot>,
    var order: MutableList<String>,
) : PendingEvChargeSnapshotStore {
    override fun find(code: String): PendingEvChargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEvChargeSnapshot) {
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

    override fun list(): MutableList<PendingEvChargeSnapshot> {
        val out = mutableListOf<PendingEvChargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEvChargeSnapshotStore(): PendingEvChargeSnapshotStore {
    return MemoryPendingEvChargeSnapshotStore(
        mutableMapOf<String, PendingEvChargeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEvChargeSnapshotStore(
    var inner: PendingEvChargeSnapshotStore,
    var cache: TtlCache<PendingEvChargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingEvChargeSnapshotStore {
    override fun find(code: String): PendingEvChargeSnapshot? {
        val hit = (cache.get(code) as PendingEvChargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEvChargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEvChargeSnapshot> = inner.list()
}

fun newCachedPendingEvChargeSnapshotStore(inner: PendingEvChargeSnapshotStore, ttl: Int, clock: Clock): PendingEvChargeSnapshotStore {
    return CachedPendingEvChargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingEvChargeSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending ev charge snapshots. Empty fields match all. */
data class PendingEvChargeSnapshotQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEvChargeSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingEvChargeSnapshot>): MutableList<PendingEvChargeSnapshot> {
        val matched = filter(items) { candidate: PendingEvChargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEvChargeSnapshots(store: PendingEvChargeSnapshotStore, query: PendingEvChargeSnapshotQuery): MutableList<PendingEvChargeSnapshot> {
    return query.apply(store.list())
}
