package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy defect snapshots by code. */
interface LegacyDefectSnapshotStore {
    fun find(code: String): LegacyDefectSnapshot?
    fun save(item: LegacyDefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDefectSnapshotStore(
    var items: MutableMap<String, LegacyDefectSnapshot>,
    var order: MutableList<String>,
) : LegacyDefectSnapshotStore {
    override fun find(code: String): LegacyDefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDefectSnapshot) {
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

    override fun list(): MutableList<LegacyDefectSnapshot> {
        val out = mutableListOf<LegacyDefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDefectSnapshotStore(): LegacyDefectSnapshotStore {
    return MemoryLegacyDefectSnapshotStore(
        mutableMapOf<String, LegacyDefectSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDefectSnapshotStore(
    var inner: LegacyDefectSnapshotStore,
    var cache: TtlCache<LegacyDefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacyDefectSnapshotStore {
    override fun find(code: String): LegacyDefectSnapshot? {
        val hit = (cache.get(code) as LegacyDefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDefectSnapshot> = inner.list()
}

fun newCachedLegacyDefectSnapshotStore(inner: LegacyDefectSnapshotStore, ttl: Int, clock: Clock): LegacyDefectSnapshotStore {
    return CachedLegacyDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacyDefectSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy defect snapshots. Empty fields match all. */
data class LegacyDefectSnapshotQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDefectSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LegacyDefectSnapshot>): MutableList<LegacyDefectSnapshot> {
        val matched = filter(items) { candidate: LegacyDefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDefectSnapshots(store: LegacyDefectSnapshotStore, query: LegacyDefectSnapshotQuery): MutableList<LegacyDefectSnapshot> {
    return query.apply(store.list())
}
