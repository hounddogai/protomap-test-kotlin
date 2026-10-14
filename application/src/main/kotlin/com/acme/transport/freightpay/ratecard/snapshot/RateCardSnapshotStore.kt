package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card snapshots by code. */
interface RateCardSnapshotStore {
    fun find(code: String): RateCardSnapshot?
    fun save(item: RateCardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardSnapshotStore(
    var items: MutableMap<String, RateCardSnapshot>,
    var order: MutableList<String>,
) : RateCardSnapshotStore {
    override fun find(code: String): RateCardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardSnapshot) {
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

    override fun list(): MutableList<RateCardSnapshot> {
        val out = mutableListOf<RateCardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardSnapshotStore(): RateCardSnapshotStore {
    return MemoryRateCardSnapshotStore(mutableMapOf<String, RateCardSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardSnapshotStore(
    var inner: RateCardSnapshotStore,
    var cache: TtlCache<RateCardSnapshot>,
    var hits: Int,
    var misses: Int,
) : RateCardSnapshotStore {
    override fun find(code: String): RateCardSnapshot? {
        val hit = (cache.get(code) as RateCardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardSnapshot> = inner.list()
}

fun newCachedRateCardSnapshotStore(inner: RateCardSnapshotStore, ttl: Int, clock: Clock): RateCardSnapshotStore {
    return CachedRateCardSnapshotStore(
        inner = inner,
        cache = newTtlCache<RateCardSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card snapshots. Empty fields match all. */
data class RateCardSnapshotQuery(
    var channel: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RateCardSnapshot>): MutableList<RateCardSnapshot> {
        val matched = filter(items) { candidate: RateCardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardSnapshots(store: RateCardSnapshotStore, query: RateCardSnapshotQuery): MutableList<RateCardSnapshot> {
    return query.apply(store.list())
}
