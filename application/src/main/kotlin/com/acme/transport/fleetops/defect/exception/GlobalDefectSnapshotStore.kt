package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global defect snapshots by code. */
interface GlobalDefectSnapshotStore {
    fun find(code: String): GlobalDefectSnapshot?
    fun save(item: GlobalDefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDefectSnapshotStore(
    var items: MutableMap<String, GlobalDefectSnapshot>,
    var order: MutableList<String>,
) : GlobalDefectSnapshotStore {
    override fun find(code: String): GlobalDefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDefectSnapshot) {
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

    override fun list(): MutableList<GlobalDefectSnapshot> {
        val out = mutableListOf<GlobalDefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDefectSnapshotStore(): GlobalDefectSnapshotStore {
    return MemoryGlobalDefectSnapshotStore(
        mutableMapOf<String, GlobalDefectSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDefectSnapshotStore(
    var inner: GlobalDefectSnapshotStore,
    var cache: TtlCache<GlobalDefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : GlobalDefectSnapshotStore {
    override fun find(code: String): GlobalDefectSnapshot? {
        val hit = (cache.get(code) as GlobalDefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDefectSnapshot> = inner.list()
}

fun newCachedGlobalDefectSnapshotStore(inner: GlobalDefectSnapshotStore, ttl: Int, clock: Clock): GlobalDefectSnapshotStore {
    return CachedGlobalDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<GlobalDefectSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global defect snapshots. Empty fields match all. */
data class GlobalDefectSnapshotQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDefectSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalDefectSnapshot>): MutableList<GlobalDefectSnapshot> {
        val matched = filter(items) { candidate: GlobalDefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDefectSnapshots(store: GlobalDefectSnapshotStore, query: GlobalDefectSnapshotQuery): MutableList<GlobalDefectSnapshot> {
    return query.apply(store.list())
}
