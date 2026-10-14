package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect snapshots by code. */
interface DefectSnapshotStore {
    fun find(code: String): DefectSnapshot?
    fun save(item: DefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectSnapshotStore(
    var items: MutableMap<String, DefectSnapshot>,
    var order: MutableList<String>,
) : DefectSnapshotStore {
    override fun find(code: String): DefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectSnapshot) {
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

    override fun list(): MutableList<DefectSnapshot> {
        val out = mutableListOf<DefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectSnapshotStore(): DefectSnapshotStore {
    return MemoryDefectSnapshotStore(mutableMapOf<String, DefectSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectSnapshotStore(
    var inner: DefectSnapshotStore,
    var cache: TtlCache<DefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefectSnapshotStore {
    override fun find(code: String): DefectSnapshot? {
        val hit = (cache.get(code) as DefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectSnapshot> = inner.list()
}

fun newCachedDefectSnapshotStore(inner: DefectSnapshotStore, ttl: Int, clock: Clock): DefectSnapshotStore {
    return CachedDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefectSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect snapshots. Empty fields match all. */
data class DefectSnapshotQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectSnapshot>): MutableList<DefectSnapshot> {
        val matched = filter(items) { candidate: DefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectSnapshots(store: DefectSnapshotStore, query: DefectSnapshotQuery): MutableList<DefectSnapshot> {
    return query.apply(store.list())
}
