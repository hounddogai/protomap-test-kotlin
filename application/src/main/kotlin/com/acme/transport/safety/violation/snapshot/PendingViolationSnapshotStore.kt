package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending violation snapshots by code. */
interface PendingViolationSnapshotStore {
    fun find(code: String): PendingViolationSnapshot?
    fun save(item: PendingViolationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingViolationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingViolationSnapshotStore(
    var items: MutableMap<String, PendingViolationSnapshot>,
    var order: MutableList<String>,
) : PendingViolationSnapshotStore {
    override fun find(code: String): PendingViolationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingViolationSnapshot) {
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

    override fun list(): MutableList<PendingViolationSnapshot> {
        val out = mutableListOf<PendingViolationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingViolationSnapshotStore(): PendingViolationSnapshotStore {
    return MemoryPendingViolationSnapshotStore(
        mutableMapOf<String, PendingViolationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingViolationSnapshotStore(
    var inner: PendingViolationSnapshotStore,
    var cache: TtlCache<PendingViolationSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingViolationSnapshotStore {
    override fun find(code: String): PendingViolationSnapshot? {
        val hit = (cache.get(code) as PendingViolationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingViolationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingViolationSnapshot> = inner.list()
}

fun newCachedPendingViolationSnapshotStore(inner: PendingViolationSnapshotStore, ttl: Int, clock: Clock): PendingViolationSnapshotStore {
    return CachedPendingViolationSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingViolationSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending violation snapshots. Empty fields match all. */
data class PendingViolationSnapshotQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingViolationSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingViolationSnapshot>): MutableList<PendingViolationSnapshot> {
        val matched = filter(items) { candidate: PendingViolationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingViolationSnapshots(store: PendingViolationSnapshotStore, query: PendingViolationSnapshotQuery): MutableList<PendingViolationSnapshot> {
    return query.apply(store.list())
}
