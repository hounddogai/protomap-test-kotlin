package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy capacity snapshots by code. */
interface LegacyCapacitySnapshotStore {
    fun find(code: String): LegacyCapacitySnapshot?
    fun save(item: LegacyCapacitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCapacitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCapacitySnapshotStore(
    var items: MutableMap<String, LegacyCapacitySnapshot>,
    var order: MutableList<String>,
) : LegacyCapacitySnapshotStore {
    override fun find(code: String): LegacyCapacitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCapacitySnapshot) {
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

    override fun list(): MutableList<LegacyCapacitySnapshot> {
        val out = mutableListOf<LegacyCapacitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCapacitySnapshotStore(): LegacyCapacitySnapshotStore {
    return MemoryLegacyCapacitySnapshotStore(
        mutableMapOf<String, LegacyCapacitySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCapacitySnapshotStore(
    var inner: LegacyCapacitySnapshotStore,
    var cache: TtlCache<LegacyCapacitySnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacyCapacitySnapshotStore {
    override fun find(code: String): LegacyCapacitySnapshot? {
        val hit = (cache.get(code) as LegacyCapacitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCapacitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCapacitySnapshot> = inner.list()
}

fun newCachedLegacyCapacitySnapshotStore(inner: LegacyCapacitySnapshotStore, ttl: Int, clock: Clock): LegacyCapacitySnapshotStore {
    return CachedLegacyCapacitySnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacyCapacitySnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy capacity snapshots. Empty fields match all. */
data class LegacyCapacitySnapshotQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCapacitySnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LegacyCapacitySnapshot>): MutableList<LegacyCapacitySnapshot> {
        val matched = filter(items) { candidate: LegacyCapacitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCapacitySnapshots(store: LegacyCapacitySnapshotStore, query: LegacyCapacitySnapshotQuery): MutableList<LegacyCapacitySnapshot> {
    return query.apply(store.list())
}
