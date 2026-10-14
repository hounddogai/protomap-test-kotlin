package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rest break snapshots by code. */
interface TieredRestBreakSnapshotStore {
    fun find(code: String): TieredRestBreakSnapshot?
    fun save(item: TieredRestBreakSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRestBreakSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRestBreakSnapshotStore(
    var items: MutableMap<String, TieredRestBreakSnapshot>,
    var order: MutableList<String>,
) : TieredRestBreakSnapshotStore {
    override fun find(code: String): TieredRestBreakSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRestBreakSnapshot) {
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

    override fun list(): MutableList<TieredRestBreakSnapshot> {
        val out = mutableListOf<TieredRestBreakSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRestBreakSnapshotStore(): TieredRestBreakSnapshotStore {
    return MemoryTieredRestBreakSnapshotStore(
        mutableMapOf<String, TieredRestBreakSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRestBreakSnapshotStore(
    var inner: TieredRestBreakSnapshotStore,
    var cache: TtlCache<TieredRestBreakSnapshot>,
    var hits: Int,
    var misses: Int,
) : TieredRestBreakSnapshotStore {
    override fun find(code: String): TieredRestBreakSnapshot? {
        val hit = (cache.get(code) as TieredRestBreakSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRestBreakSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRestBreakSnapshot> = inner.list()
}

fun newCachedTieredRestBreakSnapshotStore(inner: TieredRestBreakSnapshotStore, ttl: Int, clock: Clock): TieredRestBreakSnapshotStore {
    return CachedTieredRestBreakSnapshotStore(
        inner = inner,
        cache = newTtlCache<TieredRestBreakSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rest break snapshots. Empty fields match all. */
data class TieredRestBreakSnapshotQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRestBreakSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRestBreakSnapshot>): MutableList<TieredRestBreakSnapshot> {
        val matched = filter(items) { candidate: TieredRestBreakSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRestBreakSnapshots(store: TieredRestBreakSnapshotStore, query: TieredRestBreakSnapshotQuery): MutableList<TieredRestBreakSnapshot> {
    return query.apply(store.list())
}
