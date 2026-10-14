package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling defect snapshots by code. */
interface RollingDefectSnapshotStore {
    fun find(code: String): RollingDefectSnapshot?
    fun save(item: RollingDefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDefectSnapshotStore(
    var items: MutableMap<String, RollingDefectSnapshot>,
    var order: MutableList<String>,
) : RollingDefectSnapshotStore {
    override fun find(code: String): RollingDefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDefectSnapshot) {
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

    override fun list(): MutableList<RollingDefectSnapshot> {
        val out = mutableListOf<RollingDefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDefectSnapshotStore(): RollingDefectSnapshotStore {
    return MemoryRollingDefectSnapshotStore(
        mutableMapOf<String, RollingDefectSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDefectSnapshotStore(
    var inner: RollingDefectSnapshotStore,
    var cache: TtlCache<RollingDefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : RollingDefectSnapshotStore {
    override fun find(code: String): RollingDefectSnapshot? {
        val hit = (cache.get(code) as RollingDefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDefectSnapshot> = inner.list()
}

fun newCachedRollingDefectSnapshotStore(inner: RollingDefectSnapshotStore, ttl: Int, clock: Clock): RollingDefectSnapshotStore {
    return CachedRollingDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<RollingDefectSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling defect snapshots. Empty fields match all. */
data class RollingDefectSnapshotQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDefectSnapshot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDefectSnapshot>): MutableList<RollingDefectSnapshot> {
        val matched = filter(items) { candidate: RollingDefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDefectSnapshots(store: RollingDefectSnapshotStore, query: RollingDefectSnapshotQuery): MutableList<RollingDefectSnapshot> {
    return query.apply(store.list())
}
