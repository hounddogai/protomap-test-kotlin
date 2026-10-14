package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global service area snapshots by code. */
interface GlobalServiceAreaSnapshotStore {
    fun find(code: String): GlobalServiceAreaSnapshot?
    fun save(item: GlobalServiceAreaSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServiceAreaSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServiceAreaSnapshotStore(
    var items: MutableMap<String, GlobalServiceAreaSnapshot>,
    var order: MutableList<String>,
) : GlobalServiceAreaSnapshotStore {
    override fun find(code: String): GlobalServiceAreaSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServiceAreaSnapshot) {
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

    override fun list(): MutableList<GlobalServiceAreaSnapshot> {
        val out = mutableListOf<GlobalServiceAreaSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServiceAreaSnapshotStore(): GlobalServiceAreaSnapshotStore {
    return MemoryGlobalServiceAreaSnapshotStore(
        mutableMapOf<String, GlobalServiceAreaSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServiceAreaSnapshotStore(
    var inner: GlobalServiceAreaSnapshotStore,
    var cache: TtlCache<GlobalServiceAreaSnapshot>,
    var hits: Int,
    var misses: Int,
) : GlobalServiceAreaSnapshotStore {
    override fun find(code: String): GlobalServiceAreaSnapshot? {
        val hit = (cache.get(code) as GlobalServiceAreaSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServiceAreaSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServiceAreaSnapshot> = inner.list()
}

fun newCachedGlobalServiceAreaSnapshotStore(inner: GlobalServiceAreaSnapshotStore, ttl: Int, clock: Clock): GlobalServiceAreaSnapshotStore {
    return CachedGlobalServiceAreaSnapshotStore(
        inner = inner,
        cache = newTtlCache<GlobalServiceAreaSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service area snapshots. Empty fields match all. */
data class GlobalServiceAreaSnapshotQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServiceAreaSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalServiceAreaSnapshot>): MutableList<GlobalServiceAreaSnapshot> {
        val matched = filter(items) { candidate: GlobalServiceAreaSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServiceAreaSnapshots(store: GlobalServiceAreaSnapshotStore, query: GlobalServiceAreaSnapshotQuery): MutableList<GlobalServiceAreaSnapshot> {
    return query.apply(store.list())
}
