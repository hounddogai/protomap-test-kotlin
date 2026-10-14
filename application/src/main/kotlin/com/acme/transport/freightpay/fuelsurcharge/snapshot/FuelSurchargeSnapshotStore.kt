package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge snapshots by code. */
interface FuelSurchargeSnapshotStore {
    fun find(code: String): FuelSurchargeSnapshot?
    fun save(item: FuelSurchargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeSnapshotStore(
    var items: MutableMap<String, FuelSurchargeSnapshot>,
    var order: MutableList<String>,
) : FuelSurchargeSnapshotStore {
    override fun find(code: String): FuelSurchargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeSnapshot) {
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

    override fun list(): MutableList<FuelSurchargeSnapshot> {
        val out = mutableListOf<FuelSurchargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeSnapshotStore(): FuelSurchargeSnapshotStore {
    return MemoryFuelSurchargeSnapshotStore(
        mutableMapOf<String, FuelSurchargeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeSnapshotStore(
    var inner: FuelSurchargeSnapshotStore,
    var cache: TtlCache<FuelSurchargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeSnapshotStore {
    override fun find(code: String): FuelSurchargeSnapshot? {
        val hit = (cache.get(code) as FuelSurchargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeSnapshot> = inner.list()
}

fun newCachedFuelSurchargeSnapshotStore(inner: FuelSurchargeSnapshotStore, ttl: Int, clock: Clock): FuelSurchargeSnapshotStore {
    return CachedFuelSurchargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge snapshots. Empty fields match all. */
data class FuelSurchargeSnapshotQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeSnapshot>): MutableList<FuelSurchargeSnapshot> {
        val matched = filter(items) { candidate: FuelSurchargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeSnapshots(store: FuelSurchargeSnapshotStore, query: FuelSurchargeSnapshotQuery): MutableList<FuelSurchargeSnapshot> {
    return query.apply(store.list())
}
