package com.acme.transport.network.node.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary node snapshots by code. */
interface PrimaryNodeSnapshotStore {
    fun find(code: String): PrimaryNodeSnapshot?
    fun save(item: PrimaryNodeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNodeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNodeSnapshotStore(
    var items: MutableMap<String, PrimaryNodeSnapshot>,
    var order: MutableList<String>,
) : PrimaryNodeSnapshotStore {
    override fun find(code: String): PrimaryNodeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNodeSnapshot) {
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

    override fun list(): MutableList<PrimaryNodeSnapshot> {
        val out = mutableListOf<PrimaryNodeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNodeSnapshotStore(): PrimaryNodeSnapshotStore {
    return MemoryPrimaryNodeSnapshotStore(mutableMapOf<String, PrimaryNodeSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNodeSnapshotStore(
    var inner: PrimaryNodeSnapshotStore,
    var cache: TtlCache<PrimaryNodeSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryNodeSnapshotStore {
    override fun find(code: String): PrimaryNodeSnapshot? {
        val hit = (cache.get(code) as PrimaryNodeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNodeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNodeSnapshot> = inner.list()
}

fun newCachedPrimaryNodeSnapshotStore(inner: PrimaryNodeSnapshotStore, ttl: Int, clock: Clock): PrimaryNodeSnapshotStore {
    return CachedPrimaryNodeSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryNodeSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary node snapshots. Empty fields match all. */
data class PrimaryNodeSnapshotQuery(
    var unit: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNodeSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<PrimaryNodeSnapshot>): MutableList<PrimaryNodeSnapshot> {
        val matched = filter(items) { candidate: PrimaryNodeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNodeSnapshots(store: PrimaryNodeSnapshotStore, query: PrimaryNodeSnapshotQuery): MutableList<PrimaryNodeSnapshot> {
    return query.apply(store.list())
}
