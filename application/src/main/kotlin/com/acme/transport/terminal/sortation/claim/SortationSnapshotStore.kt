package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation snapshots by code. */
interface SortationSnapshotStore {
    fun find(code: String): SortationSnapshot?
    fun save(item: SortationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationSnapshotStore(
    var items: MutableMap<String, SortationSnapshot>,
    var order: MutableList<String>,
) : SortationSnapshotStore {
    override fun find(code: String): SortationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationSnapshot) {
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

    override fun list(): MutableList<SortationSnapshot> {
        val out = mutableListOf<SortationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationSnapshotStore(): SortationSnapshotStore {
    return MemorySortationSnapshotStore(mutableMapOf<String, SortationSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationSnapshotStore(
    var inner: SortationSnapshotStore,
    var cache: TtlCache<SortationSnapshot>,
    var hits: Int,
    var misses: Int,
) : SortationSnapshotStore {
    override fun find(code: String): SortationSnapshot? {
        val hit = (cache.get(code) as SortationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationSnapshot> = inner.list()
}

fun newCachedSortationSnapshotStore(inner: SortationSnapshotStore, ttl: Int, clock: Clock): SortationSnapshotStore {
    return CachedSortationSnapshotStore(
        inner = inner,
        cache = newTtlCache<SortationSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation snapshots. Empty fields match all. */
data class SortationSnapshotQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationSnapshot>): MutableList<SortationSnapshot> {
        val matched = filter(items) { candidate: SortationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationSnapshots(store: SortationSnapshotStore, query: SortationSnapshotQuery): MutableList<SortationSnapshot> {
    return query.apply(store.list())
}
