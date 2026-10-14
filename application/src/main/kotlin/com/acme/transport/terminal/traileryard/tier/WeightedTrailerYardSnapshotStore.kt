package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard snapshots by code. */
interface WeightedTrailerYardSnapshotStore {
    fun find(code: String): WeightedTrailerYardSnapshot?
    fun save(item: WeightedTrailerYardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardSnapshotStore(
    var items: MutableMap<String, WeightedTrailerYardSnapshot>,
    var order: MutableList<String>,
) : WeightedTrailerYardSnapshotStore {
    override fun find(code: String): WeightedTrailerYardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardSnapshot) {
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

    override fun list(): MutableList<WeightedTrailerYardSnapshot> {
        val out = mutableListOf<WeightedTrailerYardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardSnapshotStore(): WeightedTrailerYardSnapshotStore {
    return MemoryWeightedTrailerYardSnapshotStore(
        mutableMapOf<String, WeightedTrailerYardSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardSnapshotStore(
    var inner: WeightedTrailerYardSnapshotStore,
    var cache: TtlCache<WeightedTrailerYardSnapshot>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardSnapshotStore {
    override fun find(code: String): WeightedTrailerYardSnapshot? {
        val hit = (cache.get(code) as WeightedTrailerYardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardSnapshot> = inner.list()
}

fun newCachedWeightedTrailerYardSnapshotStore(inner: WeightedTrailerYardSnapshotStore, ttl: Int, clock: Clock): WeightedTrailerYardSnapshotStore {
    return CachedWeightedTrailerYardSnapshotStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard snapshots. Empty fields match all. */
data class WeightedTrailerYardSnapshotQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardSnapshot>): MutableList<WeightedTrailerYardSnapshot> {
        val matched = filter(items) { candidate: WeightedTrailerYardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardSnapshots(store: WeightedTrailerYardSnapshotStore, query: WeightedTrailerYardSnapshotQuery): MutableList<WeightedTrailerYardSnapshot> {
    return query.apply(store.list())
}
