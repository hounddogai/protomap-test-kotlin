package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability snapshots by code. */
interface AvailabilitySnapshotStore {
    fun find(code: String): AvailabilitySnapshot?
    fun save(item: AvailabilitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilitySnapshotStore(
    var items: MutableMap<String, AvailabilitySnapshot>,
    var order: MutableList<String>,
) : AvailabilitySnapshotStore {
    override fun find(code: String): AvailabilitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilitySnapshot) {
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

    override fun list(): MutableList<AvailabilitySnapshot> {
        val out = mutableListOf<AvailabilitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilitySnapshotStore(): AvailabilitySnapshotStore {
    return MemoryAvailabilitySnapshotStore(
        mutableMapOf<String, AvailabilitySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilitySnapshotStore(
    var inner: AvailabilitySnapshotStore,
    var cache: TtlCache<AvailabilitySnapshot>,
    var hits: Int,
    var misses: Int,
) : AvailabilitySnapshotStore {
    override fun find(code: String): AvailabilitySnapshot? {
        val hit = (cache.get(code) as AvailabilitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilitySnapshot> = inner.list()
}

fun newCachedAvailabilitySnapshotStore(inner: AvailabilitySnapshotStore, ttl: Int, clock: Clock): AvailabilitySnapshotStore {
    return CachedAvailabilitySnapshotStore(
        inner = inner,
        cache = newTtlCache<AvailabilitySnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability snapshots. Empty fields match all. */
data class AvailabilitySnapshotQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilitySnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilitySnapshot>): MutableList<AvailabilitySnapshot> {
        val matched = filter(items) { candidate: AvailabilitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilitySnapshots(store: AvailabilitySnapshotStore, query: AvailabilitySnapshotQuery): MutableList<AvailabilitySnapshot> {
    return query.apply(store.list())
}
