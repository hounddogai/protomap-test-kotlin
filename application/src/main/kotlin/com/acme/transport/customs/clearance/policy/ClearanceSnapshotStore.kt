package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance snapshots by code. */
interface ClearanceSnapshotStore {
    fun find(code: String): ClearanceSnapshot?
    fun save(item: ClearanceSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceSnapshotStore(
    var items: MutableMap<String, ClearanceSnapshot>,
    var order: MutableList<String>,
) : ClearanceSnapshotStore {
    override fun find(code: String): ClearanceSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceSnapshot) {
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

    override fun list(): MutableList<ClearanceSnapshot> {
        val out = mutableListOf<ClearanceSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceSnapshotStore(): ClearanceSnapshotStore {
    return MemoryClearanceSnapshotStore(mutableMapOf<String, ClearanceSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceSnapshotStore(
    var inner: ClearanceSnapshotStore,
    var cache: TtlCache<ClearanceSnapshot>,
    var hits: Int,
    var misses: Int,
) : ClearanceSnapshotStore {
    override fun find(code: String): ClearanceSnapshot? {
        val hit = (cache.get(code) as ClearanceSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceSnapshot> = inner.list()
}

fun newCachedClearanceSnapshotStore(inner: ClearanceSnapshotStore, ttl: Int, clock: Clock): ClearanceSnapshotStore {
    return CachedClearanceSnapshotStore(
        inner = inner,
        cache = newTtlCache<ClearanceSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance snapshots. Empty fields match all. */
data class ClearanceSnapshotQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceSnapshot>): MutableList<ClearanceSnapshot> {
        val matched = filter(items) { candidate: ClearanceSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceSnapshots(store: ClearanceSnapshotStore, query: ClearanceSnapshotQuery): MutableList<ClearanceSnapshot> {
    return query.apply(store.list())
}
