package com.acme.transport.fleetops.inspection.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection snapshots by code. */
interface InspectionSnapshotStore {
    fun find(code: String): InspectionSnapshot?
    fun save(item: InspectionSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionSnapshotStore(
    var items: MutableMap<String, InspectionSnapshot>,
    var order: MutableList<String>,
) : InspectionSnapshotStore {
    override fun find(code: String): InspectionSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionSnapshot) {
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

    override fun list(): MutableList<InspectionSnapshot> {
        val out = mutableListOf<InspectionSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionSnapshotStore(): InspectionSnapshotStore {
    return MemoryInspectionSnapshotStore(mutableMapOf<String, InspectionSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionSnapshotStore(
    var inner: InspectionSnapshotStore,
    var cache: TtlCache<InspectionSnapshot>,
    var hits: Int,
    var misses: Int,
) : InspectionSnapshotStore {
    override fun find(code: String): InspectionSnapshot? {
        val hit = (cache.get(code) as InspectionSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionSnapshot> = inner.list()
}

fun newCachedInspectionSnapshotStore(inner: InspectionSnapshotStore, ttl: Int, clock: Clock): InspectionSnapshotStore {
    return CachedInspectionSnapshotStore(
        inner = inner,
        cache = newTtlCache<InspectionSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection snapshots. Empty fields match all. */
data class InspectionSnapshotQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<InspectionSnapshot>): MutableList<InspectionSnapshot> {
        val matched = filter(items) { candidate: InspectionSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionSnapshots(store: InspectionSnapshotStore, query: InspectionSnapshotQuery): MutableList<InspectionSnapshot> {
    return query.apply(store.list())
}
