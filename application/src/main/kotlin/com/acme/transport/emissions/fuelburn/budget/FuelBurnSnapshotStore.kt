package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn snapshots by code. */
interface FuelBurnSnapshotStore {
    fun find(code: String): FuelBurnSnapshot?
    fun save(item: FuelBurnSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnSnapshotStore(
    var items: MutableMap<String, FuelBurnSnapshot>,
    var order: MutableList<String>,
) : FuelBurnSnapshotStore {
    override fun find(code: String): FuelBurnSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnSnapshot) {
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

    override fun list(): MutableList<FuelBurnSnapshot> {
        val out = mutableListOf<FuelBurnSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnSnapshotStore(): FuelBurnSnapshotStore {
    return MemoryFuelBurnSnapshotStore(mutableMapOf<String, FuelBurnSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnSnapshotStore(
    var inner: FuelBurnSnapshotStore,
    var cache: TtlCache<FuelBurnSnapshot>,
    var hits: Int,
    var misses: Int,
) : FuelBurnSnapshotStore {
    override fun find(code: String): FuelBurnSnapshot? {
        val hit = (cache.get(code) as FuelBurnSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnSnapshot> = inner.list()
}

fun newCachedFuelBurnSnapshotStore(inner: FuelBurnSnapshotStore, ttl: Int, clock: Clock): FuelBurnSnapshotStore {
    return CachedFuelBurnSnapshotStore(
        inner = inner,
        cache = newTtlCache<FuelBurnSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn snapshots. Empty fields match all. */
data class FuelBurnSnapshotQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnSnapshot>): MutableList<FuelBurnSnapshot> {
        val matched = filter(items) { candidate: FuelBurnSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnSnapshots(store: FuelBurnSnapshotStore, query: FuelBurnSnapshotQuery): MutableList<FuelBurnSnapshot> {
    return query.apply(store.list())
}
