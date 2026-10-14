package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rest break snapshots by code. */
interface GlobalRestBreakSnapshotStore {
    fun find(code: String): GlobalRestBreakSnapshot?
    fun save(item: GlobalRestBreakSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRestBreakSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRestBreakSnapshotStore(
    var items: MutableMap<String, GlobalRestBreakSnapshot>,
    var order: MutableList<String>,
) : GlobalRestBreakSnapshotStore {
    override fun find(code: String): GlobalRestBreakSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRestBreakSnapshot) {
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

    override fun list(): MutableList<GlobalRestBreakSnapshot> {
        val out = mutableListOf<GlobalRestBreakSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRestBreakSnapshotStore(): GlobalRestBreakSnapshotStore {
    return MemoryGlobalRestBreakSnapshotStore(
        mutableMapOf<String, GlobalRestBreakSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRestBreakSnapshotStore(
    var inner: GlobalRestBreakSnapshotStore,
    var cache: TtlCache<GlobalRestBreakSnapshot>,
    var hits: Int,
    var misses: Int,
) : GlobalRestBreakSnapshotStore {
    override fun find(code: String): GlobalRestBreakSnapshot? {
        val hit = (cache.get(code) as GlobalRestBreakSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRestBreakSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRestBreakSnapshot> = inner.list()
}

fun newCachedGlobalRestBreakSnapshotStore(inner: GlobalRestBreakSnapshotStore, ttl: Int, clock: Clock): GlobalRestBreakSnapshotStore {
    return CachedGlobalRestBreakSnapshotStore(
        inner = inner,
        cache = newTtlCache<GlobalRestBreakSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rest break snapshots. Empty fields match all. */
data class GlobalRestBreakSnapshotQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRestBreakSnapshot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRestBreakSnapshot>): MutableList<GlobalRestBreakSnapshot> {
        val matched = filter(items) { candidate: GlobalRestBreakSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRestBreakSnapshots(store: GlobalRestBreakSnapshotStore, query: GlobalRestBreakSnapshotQuery): MutableList<GlobalRestBreakSnapshot> {
    return query.apply(store.list())
}
