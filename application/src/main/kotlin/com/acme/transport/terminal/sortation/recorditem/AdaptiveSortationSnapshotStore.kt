package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive sortation snapshots by code. */
interface AdaptiveSortationSnapshotStore {
    fun find(code: String): AdaptiveSortationSnapshot?
    fun save(item: AdaptiveSortationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSortationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSortationSnapshotStore(
    var items: MutableMap<String, AdaptiveSortationSnapshot>,
    var order: MutableList<String>,
) : AdaptiveSortationSnapshotStore {
    override fun find(code: String): AdaptiveSortationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSortationSnapshot) {
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

    override fun list(): MutableList<AdaptiveSortationSnapshot> {
        val out = mutableListOf<AdaptiveSortationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSortationSnapshotStore(): AdaptiveSortationSnapshotStore {
    return MemoryAdaptiveSortationSnapshotStore(
        mutableMapOf<String, AdaptiveSortationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSortationSnapshotStore(
    var inner: AdaptiveSortationSnapshotStore,
    var cache: TtlCache<AdaptiveSortationSnapshot>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSortationSnapshotStore {
    override fun find(code: String): AdaptiveSortationSnapshot? {
        val hit = (cache.get(code) as AdaptiveSortationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSortationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSortationSnapshot> = inner.list()
}

fun newCachedAdaptiveSortationSnapshotStore(inner: AdaptiveSortationSnapshotStore, ttl: Int, clock: Clock): AdaptiveSortationSnapshotStore {
    return CachedAdaptiveSortationSnapshotStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSortationSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive sortation snapshots. Empty fields match all. */
data class AdaptiveSortationSnapshotQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSortationSnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSortationSnapshot>): MutableList<AdaptiveSortationSnapshot> {
        val matched = filter(items) { candidate: AdaptiveSortationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSortationSnapshots(store: AdaptiveSortationSnapshotStore, query: AdaptiveSortationSnapshotQuery): MutableList<AdaptiveSortationSnapshot> {
    return query.apply(store.list())
}
