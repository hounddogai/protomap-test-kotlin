package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional hub snapshots by code. */
interface RegionalHubSnapshotStore {
    fun find(code: String): RegionalHubSnapshot?
    fun save(item: RegionalHubSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalHubSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalHubSnapshotStore(
    var items: MutableMap<String, RegionalHubSnapshot>,
    var order: MutableList<String>,
) : RegionalHubSnapshotStore {
    override fun find(code: String): RegionalHubSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalHubSnapshot) {
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

    override fun list(): MutableList<RegionalHubSnapshot> {
        val out = mutableListOf<RegionalHubSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalHubSnapshotStore(): RegionalHubSnapshotStore {
    return MemoryRegionalHubSnapshotStore(mutableMapOf<String, RegionalHubSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalHubSnapshotStore(
    var inner: RegionalHubSnapshotStore,
    var cache: TtlCache<RegionalHubSnapshot>,
    var hits: Int,
    var misses: Int,
) : RegionalHubSnapshotStore {
    override fun find(code: String): RegionalHubSnapshot? {
        val hit = (cache.get(code) as RegionalHubSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalHubSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalHubSnapshot> = inner.list()
}

fun newCachedRegionalHubSnapshotStore(inner: RegionalHubSnapshotStore, ttl: Int, clock: Clock): RegionalHubSnapshotStore {
    return CachedRegionalHubSnapshotStore(
        inner = inner,
        cache = newTtlCache<RegionalHubSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional hub snapshots. Empty fields match all. */
data class RegionalHubSnapshotQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalHubSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalHubSnapshot>): MutableList<RegionalHubSnapshot> {
        val matched = filter(items) { candidate: RegionalHubSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalHubSnapshots(store: RegionalHubSnapshotStore, query: RegionalHubSnapshotQuery): MutableList<RegionalHubSnapshot> {
    return query.apply(store.list())
}
