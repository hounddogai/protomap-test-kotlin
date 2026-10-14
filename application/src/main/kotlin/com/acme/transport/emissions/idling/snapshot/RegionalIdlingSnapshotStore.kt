package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional idling snapshots by code. */
interface RegionalIdlingSnapshotStore {
    fun find(code: String): RegionalIdlingSnapshot?
    fun save(item: RegionalIdlingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIdlingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIdlingSnapshotStore(
    var items: MutableMap<String, RegionalIdlingSnapshot>,
    var order: MutableList<String>,
) : RegionalIdlingSnapshotStore {
    override fun find(code: String): RegionalIdlingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIdlingSnapshot) {
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

    override fun list(): MutableList<RegionalIdlingSnapshot> {
        val out = mutableListOf<RegionalIdlingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIdlingSnapshotStore(): RegionalIdlingSnapshotStore {
    return MemoryRegionalIdlingSnapshotStore(
        mutableMapOf<String, RegionalIdlingSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIdlingSnapshotStore(
    var inner: RegionalIdlingSnapshotStore,
    var cache: TtlCache<RegionalIdlingSnapshot>,
    var hits: Int,
    var misses: Int,
) : RegionalIdlingSnapshotStore {
    override fun find(code: String): RegionalIdlingSnapshot? {
        val hit = (cache.get(code) as RegionalIdlingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIdlingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIdlingSnapshot> = inner.list()
}

fun newCachedRegionalIdlingSnapshotStore(inner: RegionalIdlingSnapshotStore, ttl: Int, clock: Clock): RegionalIdlingSnapshotStore {
    return CachedRegionalIdlingSnapshotStore(
        inner = inner,
        cache = newTtlCache<RegionalIdlingSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional idling snapshots. Empty fields match all. */
data class RegionalIdlingSnapshotQuery(
    var category: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIdlingSnapshot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIdlingSnapshot>): MutableList<RegionalIdlingSnapshot> {
        val matched = filter(items) { candidate: RegionalIdlingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIdlingSnapshots(store: RegionalIdlingSnapshotStore, query: RegionalIdlingSnapshotQuery): MutableList<RegionalIdlingSnapshot> {
    return query.apply(store.list())
}
