package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock snapshots by code. */
interface CrossDockSnapshotStore {
    fun find(code: String): CrossDockSnapshot?
    fun save(item: CrossDockSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockSnapshotStore(
    var items: MutableMap<String, CrossDockSnapshot>,
    var order: MutableList<String>,
) : CrossDockSnapshotStore {
    override fun find(code: String): CrossDockSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockSnapshot) {
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

    override fun list(): MutableList<CrossDockSnapshot> {
        val out = mutableListOf<CrossDockSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockSnapshotStore(): CrossDockSnapshotStore {
    return MemoryCrossDockSnapshotStore(mutableMapOf<String, CrossDockSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockSnapshotStore(
    var inner: CrossDockSnapshotStore,
    var cache: TtlCache<CrossDockSnapshot>,
    var hits: Int,
    var misses: Int,
) : CrossDockSnapshotStore {
    override fun find(code: String): CrossDockSnapshot? {
        val hit = (cache.get(code) as CrossDockSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockSnapshot> = inner.list()
}

fun newCachedCrossDockSnapshotStore(inner: CrossDockSnapshotStore, ttl: Int, clock: Clock): CrossDockSnapshotStore {
    return CachedCrossDockSnapshotStore(
        inner = inner,
        cache = newTtlCache<CrossDockSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock snapshots. Empty fields match all. */
data class CrossDockSnapshotQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockSnapshot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockSnapshot>): MutableList<CrossDockSnapshot> {
        val matched = filter(items) { candidate: CrossDockSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockSnapshots(store: CrossDockSnapshotStore, query: CrossDockSnapshotQuery): MutableList<CrossDockSnapshot> {
    return query.apply(store.list())
}
