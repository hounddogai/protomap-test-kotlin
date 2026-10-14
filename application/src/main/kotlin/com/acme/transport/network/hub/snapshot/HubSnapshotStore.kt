package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub snapshots by code. */
interface HubSnapshotStore {
    fun find(code: String): HubSnapshot?
    fun save(item: HubSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubSnapshotStore(
    var items: MutableMap<String, HubSnapshot>,
    var order: MutableList<String>,
) : HubSnapshotStore {
    override fun find(code: String): HubSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubSnapshot) {
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

    override fun list(): MutableList<HubSnapshot> {
        val out = mutableListOf<HubSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubSnapshotStore(): HubSnapshotStore {
    return MemoryHubSnapshotStore(mutableMapOf<String, HubSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubSnapshotStore(
    var inner: HubSnapshotStore,
    var cache: TtlCache<HubSnapshot>,
    var hits: Int,
    var misses: Int,
) : HubSnapshotStore {
    override fun find(code: String): HubSnapshot? {
        val hit = (cache.get(code) as HubSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubSnapshot> = inner.list()
}

fun newCachedHubSnapshotStore(inner: HubSnapshotStore, ttl: Int, clock: Clock): HubSnapshotStore {
    return CachedHubSnapshotStore(
        inner = inner,
        cache = newTtlCache<HubSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub snapshots. Empty fields match all. */
data class HubSnapshotQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<HubSnapshot>): MutableList<HubSnapshot> {
        val matched = filter(items) { candidate: HubSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubSnapshots(store: HubSnapshotStore, query: HubSnapshotQuery): MutableList<HubSnapshot> {
    return query.apply(store.list())
}
