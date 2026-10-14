package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal capacity snapshots by code. */
interface SeasonalCapacitySnapshotStore {
    fun find(code: String): SeasonalCapacitySnapshot?
    fun save(item: SeasonalCapacitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCapacitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCapacitySnapshotStore(
    var items: MutableMap<String, SeasonalCapacitySnapshot>,
    var order: MutableList<String>,
) : SeasonalCapacitySnapshotStore {
    override fun find(code: String): SeasonalCapacitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCapacitySnapshot) {
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

    override fun list(): MutableList<SeasonalCapacitySnapshot> {
        val out = mutableListOf<SeasonalCapacitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCapacitySnapshotStore(): SeasonalCapacitySnapshotStore {
    return MemorySeasonalCapacitySnapshotStore(
        mutableMapOf<String, SeasonalCapacitySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCapacitySnapshotStore(
    var inner: SeasonalCapacitySnapshotStore,
    var cache: TtlCache<SeasonalCapacitySnapshot>,
    var hits: Int,
    var misses: Int,
) : SeasonalCapacitySnapshotStore {
    override fun find(code: String): SeasonalCapacitySnapshot? {
        val hit = (cache.get(code) as SeasonalCapacitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCapacitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCapacitySnapshot> = inner.list()
}

fun newCachedSeasonalCapacitySnapshotStore(inner: SeasonalCapacitySnapshotStore, ttl: Int, clock: Clock): SeasonalCapacitySnapshotStore {
    return CachedSeasonalCapacitySnapshotStore(
        inner = inner,
        cache = newTtlCache<SeasonalCapacitySnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal capacity snapshots. Empty fields match all. */
data class SeasonalCapacitySnapshotQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCapacitySnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCapacitySnapshot>): MutableList<SeasonalCapacitySnapshot> {
        val matched = filter(items) { candidate: SeasonalCapacitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCapacitySnapshots(store: SeasonalCapacitySnapshotStore, query: SeasonalCapacitySnapshotQuery): MutableList<SeasonalCapacitySnapshot> {
    return query.apply(store.list())
}
