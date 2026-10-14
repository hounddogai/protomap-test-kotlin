package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization snapshots by code. */
interface UtilizationSnapshotStore {
    fun find(code: String): UtilizationSnapshot?
    fun save(item: UtilizationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationSnapshotStore(
    var items: MutableMap<String, UtilizationSnapshot>,
    var order: MutableList<String>,
) : UtilizationSnapshotStore {
    override fun find(code: String): UtilizationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationSnapshot) {
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

    override fun list(): MutableList<UtilizationSnapshot> {
        val out = mutableListOf<UtilizationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationSnapshotStore(): UtilizationSnapshotStore {
    return MemoryUtilizationSnapshotStore(mutableMapOf<String, UtilizationSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationSnapshotStore(
    var inner: UtilizationSnapshotStore,
    var cache: TtlCache<UtilizationSnapshot>,
    var hits: Int,
    var misses: Int,
) : UtilizationSnapshotStore {
    override fun find(code: String): UtilizationSnapshot? {
        val hit = (cache.get(code) as UtilizationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationSnapshot> = inner.list()
}

fun newCachedUtilizationSnapshotStore(inner: UtilizationSnapshotStore, ttl: Int, clock: Clock): UtilizationSnapshotStore {
    return CachedUtilizationSnapshotStore(
        inner = inner,
        cache = newTtlCache<UtilizationSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization snapshots. Empty fields match all. */
data class UtilizationSnapshotQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<UtilizationSnapshot>): MutableList<UtilizationSnapshot> {
        val matched = filter(items) { candidate: UtilizationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationSnapshots(store: UtilizationSnapshotStore, query: UtilizationSnapshotQuery): MutableList<UtilizationSnapshot> {
    return query.apply(store.list())
}
