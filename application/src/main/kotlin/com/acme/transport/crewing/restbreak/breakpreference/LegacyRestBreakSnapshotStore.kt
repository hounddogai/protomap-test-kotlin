package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rest break snapshots by code. */
interface LegacyRestBreakSnapshotStore {
    fun find(code: String): LegacyRestBreakSnapshot?
    fun save(item: LegacyRestBreakSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRestBreakSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRestBreakSnapshotStore(
    var items: MutableMap<String, LegacyRestBreakSnapshot>,
    var order: MutableList<String>,
) : LegacyRestBreakSnapshotStore {
    override fun find(code: String): LegacyRestBreakSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRestBreakSnapshot) {
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

    override fun list(): MutableList<LegacyRestBreakSnapshot> {
        val out = mutableListOf<LegacyRestBreakSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRestBreakSnapshotStore(): LegacyRestBreakSnapshotStore {
    return MemoryLegacyRestBreakSnapshotStore(
        mutableMapOf<String, LegacyRestBreakSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRestBreakSnapshotStore(
    var inner: LegacyRestBreakSnapshotStore,
    var cache: TtlCache<LegacyRestBreakSnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacyRestBreakSnapshotStore {
    override fun find(code: String): LegacyRestBreakSnapshot? {
        val hit = (cache.get(code) as LegacyRestBreakSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRestBreakSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRestBreakSnapshot> = inner.list()
}

fun newCachedLegacyRestBreakSnapshotStore(inner: LegacyRestBreakSnapshotStore, ttl: Int, clock: Clock): LegacyRestBreakSnapshotStore {
    return CachedLegacyRestBreakSnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacyRestBreakSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rest break snapshots. Empty fields match all. */
data class LegacyRestBreakSnapshotQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRestBreakSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRestBreakSnapshot>): MutableList<LegacyRestBreakSnapshot> {
        val matched = filter(items) { candidate: LegacyRestBreakSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRestBreakSnapshots(store: LegacyRestBreakSnapshotStore, query: LegacyRestBreakSnapshotQuery): MutableList<LegacyRestBreakSnapshot> {
    return query.apply(store.list())
}
