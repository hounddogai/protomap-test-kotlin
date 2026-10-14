package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal rate card snapshots by code. */
interface SeasonalRateCardSnapshotStore {
    fun find(code: String): SeasonalRateCardSnapshot?
    fun save(item: SeasonalRateCardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRateCardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRateCardSnapshotStore(
    var items: MutableMap<String, SeasonalRateCardSnapshot>,
    var order: MutableList<String>,
) : SeasonalRateCardSnapshotStore {
    override fun find(code: String): SeasonalRateCardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRateCardSnapshot) {
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

    override fun list(): MutableList<SeasonalRateCardSnapshot> {
        val out = mutableListOf<SeasonalRateCardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRateCardSnapshotStore(): SeasonalRateCardSnapshotStore {
    return MemorySeasonalRateCardSnapshotStore(
        mutableMapOf<String, SeasonalRateCardSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRateCardSnapshotStore(
    var inner: SeasonalRateCardSnapshotStore,
    var cache: TtlCache<SeasonalRateCardSnapshot>,
    var hits: Int,
    var misses: Int,
) : SeasonalRateCardSnapshotStore {
    override fun find(code: String): SeasonalRateCardSnapshot? {
        val hit = (cache.get(code) as SeasonalRateCardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRateCardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRateCardSnapshot> = inner.list()
}

fun newCachedSeasonalRateCardSnapshotStore(inner: SeasonalRateCardSnapshotStore, ttl: Int, clock: Clock): SeasonalRateCardSnapshotStore {
    return CachedSeasonalRateCardSnapshotStore(
        inner = inner,
        cache = newTtlCache<SeasonalRateCardSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal rate card snapshots. Empty fields match all. */
data class SeasonalRateCardSnapshotQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRateCardSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalRateCardSnapshot>): MutableList<SeasonalRateCardSnapshot> {
        val matched = filter(items) { candidate: SeasonalRateCardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRateCardSnapshots(store: SeasonalRateCardSnapshotStore, query: SeasonalRateCardSnapshotQuery): MutableList<SeasonalRateCardSnapshot> {
    return query.apply(store.list())
}
