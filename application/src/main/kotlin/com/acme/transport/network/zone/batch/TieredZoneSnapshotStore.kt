package com.acme.transport.network.zone.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered zone snapshots by code. */
interface TieredZoneSnapshotStore {
    fun find(code: String): TieredZoneSnapshot?
    fun save(item: TieredZoneSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredZoneSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredZoneSnapshotStore(
    var items: MutableMap<String, TieredZoneSnapshot>,
    var order: MutableList<String>,
) : TieredZoneSnapshotStore {
    override fun find(code: String): TieredZoneSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredZoneSnapshot) {
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

    override fun list(): MutableList<TieredZoneSnapshot> {
        val out = mutableListOf<TieredZoneSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredZoneSnapshotStore(): TieredZoneSnapshotStore {
    return MemoryTieredZoneSnapshotStore(mutableMapOf<String, TieredZoneSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredZoneSnapshotStore(
    var inner: TieredZoneSnapshotStore,
    var cache: TtlCache<TieredZoneSnapshot>,
    var hits: Int,
    var misses: Int,
) : TieredZoneSnapshotStore {
    override fun find(code: String): TieredZoneSnapshot? {
        val hit = (cache.get(code) as TieredZoneSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredZoneSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredZoneSnapshot> = inner.list()
}

fun newCachedTieredZoneSnapshotStore(inner: TieredZoneSnapshotStore, ttl: Int, clock: Clock): TieredZoneSnapshotStore {
    return CachedTieredZoneSnapshotStore(
        inner = inner,
        cache = newTtlCache<TieredZoneSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered zone snapshots. Empty fields match all. */
data class TieredZoneSnapshotQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredZoneSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredZoneSnapshot>): MutableList<TieredZoneSnapshot> {
        val matched = filter(items) { candidate: TieredZoneSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredZoneSnapshots(store: TieredZoneSnapshotStore, query: TieredZoneSnapshotQuery): MutableList<TieredZoneSnapshot> {
    return query.apply(store.list())
}
