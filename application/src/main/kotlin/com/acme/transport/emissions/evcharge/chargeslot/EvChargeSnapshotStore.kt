package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge snapshots by code. */
interface EvChargeSnapshotStore {
    fun find(code: String): EvChargeSnapshot?
    fun save(item: EvChargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeSnapshotStore(
    var items: MutableMap<String, EvChargeSnapshot>,
    var order: MutableList<String>,
) : EvChargeSnapshotStore {
    override fun find(code: String): EvChargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeSnapshot) {
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

    override fun list(): MutableList<EvChargeSnapshot> {
        val out = mutableListOf<EvChargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeSnapshotStore(): EvChargeSnapshotStore {
    return MemoryEvChargeSnapshotStore(mutableMapOf<String, EvChargeSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeSnapshotStore(
    var inner: EvChargeSnapshotStore,
    var cache: TtlCache<EvChargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : EvChargeSnapshotStore {
    override fun find(code: String): EvChargeSnapshot? {
        val hit = (cache.get(code) as EvChargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeSnapshot> = inner.list()
}

fun newCachedEvChargeSnapshotStore(inner: EvChargeSnapshotStore, ttl: Int, clock: Clock): EvChargeSnapshotStore {
    return CachedEvChargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<EvChargeSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge snapshots. Empty fields match all. */
data class EvChargeSnapshotQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeSnapshot>): MutableList<EvChargeSnapshot> {
        val matched = filter(items) { candidate: EvChargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeSnapshots(store: EvChargeSnapshotStore, query: EvChargeSnapshotQuery): MutableList<EvChargeSnapshot> {
    return query.apply(store.list())
}
