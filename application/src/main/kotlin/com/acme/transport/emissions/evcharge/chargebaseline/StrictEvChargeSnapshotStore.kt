package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge snapshots by code. */
interface StrictEvChargeSnapshotStore {
    fun find(code: String): StrictEvChargeSnapshot?
    fun save(item: StrictEvChargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeSnapshotStore(
    var items: MutableMap<String, StrictEvChargeSnapshot>,
    var order: MutableList<String>,
) : StrictEvChargeSnapshotStore {
    override fun find(code: String): StrictEvChargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeSnapshot) {
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

    override fun list(): MutableList<StrictEvChargeSnapshot> {
        val out = mutableListOf<StrictEvChargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeSnapshotStore(): StrictEvChargeSnapshotStore {
    return MemoryStrictEvChargeSnapshotStore(
        mutableMapOf<String, StrictEvChargeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeSnapshotStore(
    var inner: StrictEvChargeSnapshotStore,
    var cache: TtlCache<StrictEvChargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeSnapshotStore {
    override fun find(code: String): StrictEvChargeSnapshot? {
        val hit = (cache.get(code) as StrictEvChargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeSnapshot> = inner.list()
}

fun newCachedStrictEvChargeSnapshotStore(inner: StrictEvChargeSnapshotStore, ttl: Int, clock: Clock): StrictEvChargeSnapshotStore {
    return CachedStrictEvChargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge snapshots. Empty fields match all. */
data class StrictEvChargeSnapshotQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictEvChargeSnapshot>): MutableList<StrictEvChargeSnapshot> {
        val matched = filter(items) { candidate: StrictEvChargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeSnapshots(store: StrictEvChargeSnapshotStore, query: StrictEvChargeSnapshotQuery): MutableList<StrictEvChargeSnapshot> {
    return query.apply(store.list())
}
