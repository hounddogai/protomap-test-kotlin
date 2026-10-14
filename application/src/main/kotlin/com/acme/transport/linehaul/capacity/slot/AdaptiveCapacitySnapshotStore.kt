package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive capacity snapshots by code. */
interface AdaptiveCapacitySnapshotStore {
    fun find(code: String): AdaptiveCapacitySnapshot?
    fun save(item: AdaptiveCapacitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCapacitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCapacitySnapshotStore(
    var items: MutableMap<String, AdaptiveCapacitySnapshot>,
    var order: MutableList<String>,
) : AdaptiveCapacitySnapshotStore {
    override fun find(code: String): AdaptiveCapacitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCapacitySnapshot) {
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

    override fun list(): MutableList<AdaptiveCapacitySnapshot> {
        val out = mutableListOf<AdaptiveCapacitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCapacitySnapshotStore(): AdaptiveCapacitySnapshotStore {
    return MemoryAdaptiveCapacitySnapshotStore(
        mutableMapOf<String, AdaptiveCapacitySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCapacitySnapshotStore(
    var inner: AdaptiveCapacitySnapshotStore,
    var cache: TtlCache<AdaptiveCapacitySnapshot>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCapacitySnapshotStore {
    override fun find(code: String): AdaptiveCapacitySnapshot? {
        val hit = (cache.get(code) as AdaptiveCapacitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCapacitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCapacitySnapshot> = inner.list()
}

fun newCachedAdaptiveCapacitySnapshotStore(inner: AdaptiveCapacitySnapshotStore, ttl: Int, clock: Clock): AdaptiveCapacitySnapshotStore {
    return CachedAdaptiveCapacitySnapshotStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCapacitySnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive capacity snapshots. Empty fields match all. */
data class AdaptiveCapacitySnapshotQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCapacitySnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCapacitySnapshot>): MutableList<AdaptiveCapacitySnapshot> {
        val matched = filter(items) { candidate: AdaptiveCapacitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCapacitySnapshots(store: AdaptiveCapacitySnapshotStore, query: AdaptiveCapacitySnapshotQuery): MutableList<AdaptiveCapacitySnapshot> {
    return query.apply(store.list())
}
