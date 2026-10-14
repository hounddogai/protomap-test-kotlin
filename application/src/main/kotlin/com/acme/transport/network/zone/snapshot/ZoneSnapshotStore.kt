package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone snapshots by code. */
interface ZoneSnapshotStore {
    fun find(code: String): ZoneSnapshot?
    fun save(item: ZoneSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneSnapshotStore(
    var items: MutableMap<String, ZoneSnapshot>,
    var order: MutableList<String>,
) : ZoneSnapshotStore {
    override fun find(code: String): ZoneSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneSnapshot) {
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

    override fun list(): MutableList<ZoneSnapshot> {
        val out = mutableListOf<ZoneSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneSnapshotStore(): ZoneSnapshotStore {
    return MemoryZoneSnapshotStore(mutableMapOf<String, ZoneSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneSnapshotStore(
    var inner: ZoneSnapshotStore,
    var cache: TtlCache<ZoneSnapshot>,
    var hits: Int,
    var misses: Int,
) : ZoneSnapshotStore {
    override fun find(code: String): ZoneSnapshot? {
        val hit = (cache.get(code) as ZoneSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneSnapshot> = inner.list()
}

fun newCachedZoneSnapshotStore(inner: ZoneSnapshotStore, ttl: Int, clock: Clock): ZoneSnapshotStore {
    return CachedZoneSnapshotStore(
        inner = inner,
        cache = newTtlCache<ZoneSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone snapshots. Empty fields match all. */
data class ZoneSnapshotQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneSnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneSnapshot>): MutableList<ZoneSnapshot> {
        val matched = filter(items) { candidate: ZoneSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneSnapshots(store: ZoneSnapshotStore, query: ZoneSnapshotQuery): MutableList<ZoneSnapshot> {
    return query.apply(store.list())
}
