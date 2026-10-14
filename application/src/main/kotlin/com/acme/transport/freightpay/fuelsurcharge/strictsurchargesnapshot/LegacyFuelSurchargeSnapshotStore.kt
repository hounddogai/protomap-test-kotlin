package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy fuel surcharge snapshots by code. */
interface LegacyFuelSurchargeSnapshotStore {
    fun find(code: String): LegacyFuelSurchargeSnapshot?
    fun save(item: LegacyFuelSurchargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFuelSurchargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFuelSurchargeSnapshotStore(
    var items: MutableMap<String, LegacyFuelSurchargeSnapshot>,
    var order: MutableList<String>,
) : LegacyFuelSurchargeSnapshotStore {
    override fun find(code: String): LegacyFuelSurchargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFuelSurchargeSnapshot) {
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

    override fun list(): MutableList<LegacyFuelSurchargeSnapshot> {
        val out = mutableListOf<LegacyFuelSurchargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFuelSurchargeSnapshotStore(): LegacyFuelSurchargeSnapshotStore {
    return MemoryLegacyFuelSurchargeSnapshotStore(
        mutableMapOf<String, LegacyFuelSurchargeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFuelSurchargeSnapshotStore(
    var inner: LegacyFuelSurchargeSnapshotStore,
    var cache: TtlCache<LegacyFuelSurchargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacyFuelSurchargeSnapshotStore {
    override fun find(code: String): LegacyFuelSurchargeSnapshot? {
        val hit = (cache.get(code) as LegacyFuelSurchargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFuelSurchargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFuelSurchargeSnapshot> = inner.list()
}

fun newCachedLegacyFuelSurchargeSnapshotStore(inner: LegacyFuelSurchargeSnapshotStore, ttl: Int, clock: Clock): LegacyFuelSurchargeSnapshotStore {
    return CachedLegacyFuelSurchargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacyFuelSurchargeSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy fuel surcharge snapshots. Empty fields match all. */
data class LegacyFuelSurchargeSnapshotQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFuelSurchargeSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyFuelSurchargeSnapshot>): MutableList<LegacyFuelSurchargeSnapshot> {
        val matched = filter(items) { candidate: LegacyFuelSurchargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFuelSurchargeSnapshots(store: LegacyFuelSurchargeSnapshotStore, query: LegacyFuelSurchargeSnapshotQuery): MutableList<LegacyFuelSurchargeSnapshot> {
    return query.apply(store.list())
}
