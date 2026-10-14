package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff snapshots by code. */
interface CutoffSnapshotStore {
    fun find(code: String): CutoffSnapshot?
    fun save(item: CutoffSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffSnapshotStore(
    var items: MutableMap<String, CutoffSnapshot>,
    var order: MutableList<String>,
) : CutoffSnapshotStore {
    override fun find(code: String): CutoffSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffSnapshot) {
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

    override fun list(): MutableList<CutoffSnapshot> {
        val out = mutableListOf<CutoffSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffSnapshotStore(): CutoffSnapshotStore {
    return MemoryCutoffSnapshotStore(mutableMapOf<String, CutoffSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffSnapshotStore(
    var inner: CutoffSnapshotStore,
    var cache: TtlCache<CutoffSnapshot>,
    var hits: Int,
    var misses: Int,
) : CutoffSnapshotStore {
    override fun find(code: String): CutoffSnapshot? {
        val hit = (cache.get(code) as CutoffSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffSnapshot> = inner.list()
}

fun newCachedCutoffSnapshotStore(inner: CutoffSnapshotStore, ttl: Int, clock: Clock): CutoffSnapshotStore {
    return CachedCutoffSnapshotStore(
        inner = inner,
        cache = newTtlCache<CutoffSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff snapshots. Empty fields match all. */
data class CutoffSnapshotQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CutoffSnapshot>): MutableList<CutoffSnapshot> {
        val matched = filter(items) { candidate: CutoffSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffSnapshots(store: CutoffSnapshotStore, query: CutoffSnapshotQuery): MutableList<CutoffSnapshot> {
    return query.apply(store.list())
}
