package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel snapshots by code. */
interface ParcelSnapshotStore {
    fun find(code: String): ParcelSnapshot?
    fun save(item: ParcelSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelSnapshotStore(
    var items: MutableMap<String, ParcelSnapshot>,
    var order: MutableList<String>,
) : ParcelSnapshotStore {
    override fun find(code: String): ParcelSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelSnapshot) {
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

    override fun list(): MutableList<ParcelSnapshot> {
        val out = mutableListOf<ParcelSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelSnapshotStore(): ParcelSnapshotStore {
    return MemoryParcelSnapshotStore(mutableMapOf<String, ParcelSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelSnapshotStore(
    var inner: ParcelSnapshotStore,
    var cache: TtlCache<ParcelSnapshot>,
    var hits: Int,
    var misses: Int,
) : ParcelSnapshotStore {
    override fun find(code: String): ParcelSnapshot? {
        val hit = (cache.get(code) as ParcelSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelSnapshot> = inner.list()
}

fun newCachedParcelSnapshotStore(inner: ParcelSnapshotStore, ttl: Int, clock: Clock): ParcelSnapshotStore {
    return CachedParcelSnapshotStore(
        inner = inner,
        cache = newTtlCache<ParcelSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel snapshots. Empty fields match all. */
data class ParcelSnapshotQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelSnapshot): Boolean {
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

    fun apply(items: MutableList<ParcelSnapshot>): MutableList<ParcelSnapshot> {
        val matched = filter(items) { candidate: ParcelSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelSnapshots(store: ParcelSnapshotStore, query: ParcelSnapshotQuery): MutableList<ParcelSnapshot> {
    return query.apply(store.list())
}
