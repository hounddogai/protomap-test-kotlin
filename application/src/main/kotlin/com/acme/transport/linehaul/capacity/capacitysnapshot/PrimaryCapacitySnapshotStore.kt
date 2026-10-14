package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary capacity snapshots by code. */
interface PrimaryCapacitySnapshotStore {
    fun find(code: String): PrimaryCapacitySnapshot?
    fun save(item: PrimaryCapacitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCapacitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCapacitySnapshotStore(
    var items: MutableMap<String, PrimaryCapacitySnapshot>,
    var order: MutableList<String>,
) : PrimaryCapacitySnapshotStore {
    override fun find(code: String): PrimaryCapacitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCapacitySnapshot) {
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

    override fun list(): MutableList<PrimaryCapacitySnapshot> {
        val out = mutableListOf<PrimaryCapacitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCapacitySnapshotStore(): PrimaryCapacitySnapshotStore {
    return MemoryPrimaryCapacitySnapshotStore(
        mutableMapOf<String, PrimaryCapacitySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCapacitySnapshotStore(
    var inner: PrimaryCapacitySnapshotStore,
    var cache: TtlCache<PrimaryCapacitySnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryCapacitySnapshotStore {
    override fun find(code: String): PrimaryCapacitySnapshot? {
        val hit = (cache.get(code) as PrimaryCapacitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCapacitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCapacitySnapshot> = inner.list()
}

fun newCachedPrimaryCapacitySnapshotStore(inner: PrimaryCapacitySnapshotStore, ttl: Int, clock: Clock): PrimaryCapacitySnapshotStore {
    return CachedPrimaryCapacitySnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryCapacitySnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary capacity snapshots. Empty fields match all. */
data class PrimaryCapacitySnapshotQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCapacitySnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCapacitySnapshot>): MutableList<PrimaryCapacitySnapshot> {
        val matched = filter(items) { candidate: PrimaryCapacitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCapacitySnapshots(store: PrimaryCapacitySnapshotStore, query: PrimaryCapacitySnapshotQuery): MutableList<PrimaryCapacitySnapshot> {
    return query.apply(store.list())
}
