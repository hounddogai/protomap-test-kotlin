package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary sortation snapshots by code. */
interface PrimarySortationSnapshotStore {
    fun find(code: String): PrimarySortationSnapshot?
    fun save(item: PrimarySortationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimarySortationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimarySortationSnapshotStore(
    var items: MutableMap<String, PrimarySortationSnapshot>,
    var order: MutableList<String>,
) : PrimarySortationSnapshotStore {
    override fun find(code: String): PrimarySortationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimarySortationSnapshot) {
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

    override fun list(): MutableList<PrimarySortationSnapshot> {
        val out = mutableListOf<PrimarySortationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimarySortationSnapshotStore(): PrimarySortationSnapshotStore {
    return MemoryPrimarySortationSnapshotStore(
        mutableMapOf<String, PrimarySortationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimarySortationSnapshotStore(
    var inner: PrimarySortationSnapshotStore,
    var cache: TtlCache<PrimarySortationSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimarySortationSnapshotStore {
    override fun find(code: String): PrimarySortationSnapshot? {
        val hit = (cache.get(code) as PrimarySortationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimarySortationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimarySortationSnapshot> = inner.list()
}

fun newCachedPrimarySortationSnapshotStore(inner: PrimarySortationSnapshotStore, ttl: Int, clock: Clock): PrimarySortationSnapshotStore {
    return CachedPrimarySortationSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimarySortationSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary sortation snapshots. Empty fields match all. */
data class PrimarySortationSnapshotQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimarySortationSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PrimarySortationSnapshot>): MutableList<PrimarySortationSnapshot> {
        val matched = filter(items) { candidate: PrimarySortationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimarySortationSnapshots(store: PrimarySortationSnapshotStore, query: PrimarySortationSnapshotQuery): MutableList<PrimarySortationSnapshot> {
    return query.apply(store.list())
}
