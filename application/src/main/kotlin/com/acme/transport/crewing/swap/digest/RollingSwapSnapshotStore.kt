package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap snapshots by code. */
interface RollingSwapSnapshotStore {
    fun find(code: String): RollingSwapSnapshot?
    fun save(item: RollingSwapSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapSnapshotStore(
    var items: MutableMap<String, RollingSwapSnapshot>,
    var order: MutableList<String>,
) : RollingSwapSnapshotStore {
    override fun find(code: String): RollingSwapSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapSnapshot) {
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

    override fun list(): MutableList<RollingSwapSnapshot> {
        val out = mutableListOf<RollingSwapSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapSnapshotStore(): RollingSwapSnapshotStore {
    return MemoryRollingSwapSnapshotStore(mutableMapOf<String, RollingSwapSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapSnapshotStore(
    var inner: RollingSwapSnapshotStore,
    var cache: TtlCache<RollingSwapSnapshot>,
    var hits: Int,
    var misses: Int,
) : RollingSwapSnapshotStore {
    override fun find(code: String): RollingSwapSnapshot? {
        val hit = (cache.get(code) as RollingSwapSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapSnapshot> = inner.list()
}

fun newCachedRollingSwapSnapshotStore(inner: RollingSwapSnapshotStore, ttl: Int, clock: Clock): RollingSwapSnapshotStore {
    return CachedRollingSwapSnapshotStore(
        inner = inner,
        cache = newTtlCache<RollingSwapSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap snapshots. Empty fields match all. */
data class RollingSwapSnapshotQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapSnapshot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapSnapshot>): MutableList<RollingSwapSnapshot> {
        val matched = filter(items) { candidate: RollingSwapSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapSnapshots(store: RollingSwapSnapshotStore, query: RollingSwapSnapshotQuery): MutableList<RollingSwapSnapshot> {
    return query.apply(store.list())
}
