package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted sortation snapshots by code. */
interface WeightedSortationSnapshotStore {
    fun find(code: String): WeightedSortationSnapshot?
    fun save(item: WeightedSortationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSortationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSortationSnapshotStore(
    var items: MutableMap<String, WeightedSortationSnapshot>,
    var order: MutableList<String>,
) : WeightedSortationSnapshotStore {
    override fun find(code: String): WeightedSortationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSortationSnapshot) {
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

    override fun list(): MutableList<WeightedSortationSnapshot> {
        val out = mutableListOf<WeightedSortationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSortationSnapshotStore(): WeightedSortationSnapshotStore {
    return MemoryWeightedSortationSnapshotStore(
        mutableMapOf<String, WeightedSortationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSortationSnapshotStore(
    var inner: WeightedSortationSnapshotStore,
    var cache: TtlCache<WeightedSortationSnapshot>,
    var hits: Int,
    var misses: Int,
) : WeightedSortationSnapshotStore {
    override fun find(code: String): WeightedSortationSnapshot? {
        val hit = (cache.get(code) as WeightedSortationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSortationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSortationSnapshot> = inner.list()
}

fun newCachedWeightedSortationSnapshotStore(inner: WeightedSortationSnapshotStore, ttl: Int, clock: Clock): WeightedSortationSnapshotStore {
    return CachedWeightedSortationSnapshotStore(
        inner = inner,
        cache = newTtlCache<WeightedSortationSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted sortation snapshots. Empty fields match all. */
data class WeightedSortationSnapshotQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSortationSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedSortationSnapshot>): MutableList<WeightedSortationSnapshot> {
        val matched = filter(items) { candidate: WeightedSortationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSortationSnapshots(store: WeightedSortationSnapshotStore, query: WeightedSortationSnapshotQuery): MutableList<WeightedSortationSnapshot> {
    return query.apply(store.list())
}
