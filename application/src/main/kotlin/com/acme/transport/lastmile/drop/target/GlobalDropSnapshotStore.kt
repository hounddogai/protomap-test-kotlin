package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global drop snapshots by code. */
interface GlobalDropSnapshotStore {
    fun find(code: String): GlobalDropSnapshot?
    fun save(item: GlobalDropSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDropSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDropSnapshotStore(
    var items: MutableMap<String, GlobalDropSnapshot>,
    var order: MutableList<String>,
) : GlobalDropSnapshotStore {
    override fun find(code: String): GlobalDropSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDropSnapshot) {
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

    override fun list(): MutableList<GlobalDropSnapshot> {
        val out = mutableListOf<GlobalDropSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDropSnapshotStore(): GlobalDropSnapshotStore {
    return MemoryGlobalDropSnapshotStore(mutableMapOf<String, GlobalDropSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDropSnapshotStore(
    var inner: GlobalDropSnapshotStore,
    var cache: TtlCache<GlobalDropSnapshot>,
    var hits: Int,
    var misses: Int,
) : GlobalDropSnapshotStore {
    override fun find(code: String): GlobalDropSnapshot? {
        val hit = (cache.get(code) as GlobalDropSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDropSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDropSnapshot> = inner.list()
}

fun newCachedGlobalDropSnapshotStore(inner: GlobalDropSnapshotStore, ttl: Int, clock: Clock): GlobalDropSnapshotStore {
    return CachedGlobalDropSnapshotStore(
        inner = inner,
        cache = newTtlCache<GlobalDropSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global drop snapshots. Empty fields match all. */
data class GlobalDropSnapshotQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDropSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDropSnapshot>): MutableList<GlobalDropSnapshot> {
        val matched = filter(items) { candidate: GlobalDropSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDropSnapshots(store: GlobalDropSnapshotStore, query: GlobalDropSnapshotQuery): MutableList<GlobalDropSnapshot> {
    return query.apply(store.list())
}
