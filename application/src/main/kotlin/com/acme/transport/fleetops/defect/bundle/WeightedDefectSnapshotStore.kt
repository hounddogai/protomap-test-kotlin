package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted defect snapshots by code. */
interface WeightedDefectSnapshotStore {
    fun find(code: String): WeightedDefectSnapshot?
    fun save(item: WeightedDefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDefectSnapshotStore(
    var items: MutableMap<String, WeightedDefectSnapshot>,
    var order: MutableList<String>,
) : WeightedDefectSnapshotStore {
    override fun find(code: String): WeightedDefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDefectSnapshot) {
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

    override fun list(): MutableList<WeightedDefectSnapshot> {
        val out = mutableListOf<WeightedDefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDefectSnapshotStore(): WeightedDefectSnapshotStore {
    return MemoryWeightedDefectSnapshotStore(
        mutableMapOf<String, WeightedDefectSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDefectSnapshotStore(
    var inner: WeightedDefectSnapshotStore,
    var cache: TtlCache<WeightedDefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : WeightedDefectSnapshotStore {
    override fun find(code: String): WeightedDefectSnapshot? {
        val hit = (cache.get(code) as WeightedDefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDefectSnapshot> = inner.list()
}

fun newCachedWeightedDefectSnapshotStore(inner: WeightedDefectSnapshotStore, ttl: Int, clock: Clock): WeightedDefectSnapshotStore {
    return CachedWeightedDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<WeightedDefectSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted defect snapshots. Empty fields match all. */
data class WeightedDefectSnapshotQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDefectSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDefectSnapshot>): MutableList<WeightedDefectSnapshot> {
        val matched = filter(items) { candidate: WeightedDefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDefectSnapshots(store: WeightedDefectSnapshotStore, query: WeightedDefectSnapshotQuery): MutableList<WeightedDefectSnapshot> {
    return query.apply(store.list())
}
