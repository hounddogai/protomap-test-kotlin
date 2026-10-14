package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending tender snapshots by code. */
interface PendingTenderSnapshotStore {
    fun find(code: String): PendingTenderSnapshot?
    fun save(item: PendingTenderSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTenderSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTenderSnapshotStore(
    var items: MutableMap<String, PendingTenderSnapshot>,
    var order: MutableList<String>,
) : PendingTenderSnapshotStore {
    override fun find(code: String): PendingTenderSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTenderSnapshot) {
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

    override fun list(): MutableList<PendingTenderSnapshot> {
        val out = mutableListOf<PendingTenderSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTenderSnapshotStore(): PendingTenderSnapshotStore {
    return MemoryPendingTenderSnapshotStore(
        mutableMapOf<String, PendingTenderSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTenderSnapshotStore(
    var inner: PendingTenderSnapshotStore,
    var cache: TtlCache<PendingTenderSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingTenderSnapshotStore {
    override fun find(code: String): PendingTenderSnapshot? {
        val hit = (cache.get(code) as PendingTenderSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTenderSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTenderSnapshot> = inner.list()
}

fun newCachedPendingTenderSnapshotStore(inner: PendingTenderSnapshotStore, ttl: Int, clock: Clock): PendingTenderSnapshotStore {
    return CachedPendingTenderSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingTenderSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tender snapshots. Empty fields match all. */
data class PendingTenderSnapshotQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTenderSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTenderSnapshot>): MutableList<PendingTenderSnapshot> {
        val matched = filter(items) { candidate: PendingTenderSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTenderSnapshots(store: PendingTenderSnapshotStore, query: PendingTenderSnapshotQuery): MutableList<PendingTenderSnapshot> {
    return query.apply(store.list())
}
