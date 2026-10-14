package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal parcel ledgers by code. */
interface SeasonalParcelLedgerStore {
    fun find(code: String): SeasonalParcelLedger?
    fun save(item: SeasonalParcelLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalParcelLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalParcelLedgerStore(
    var items: MutableMap<String, SeasonalParcelLedger>,
    var order: MutableList<String>,
) : SeasonalParcelLedgerStore {
    override fun find(code: String): SeasonalParcelLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalParcelLedger) {
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

    override fun list(): MutableList<SeasonalParcelLedger> {
        val out = mutableListOf<SeasonalParcelLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalParcelLedgerStore(): SeasonalParcelLedgerStore {
    return MemorySeasonalParcelLedgerStore(
        mutableMapOf<String, SeasonalParcelLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalParcelLedgerStore(
    var inner: SeasonalParcelLedgerStore,
    var cache: TtlCache<SeasonalParcelLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalParcelLedgerStore {
    override fun find(code: String): SeasonalParcelLedger? {
        val hit = (cache.get(code) as SeasonalParcelLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalParcelLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalParcelLedger> = inner.list()
}

fun newCachedSeasonalParcelLedgerStore(inner: SeasonalParcelLedgerStore, ttl: Int, clock: Clock): SeasonalParcelLedgerStore {
    return CachedSeasonalParcelLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalParcelLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal parcel ledgers. Empty fields match all. */
data class SeasonalParcelLedgerQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalParcelLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalParcelLedger>): MutableList<SeasonalParcelLedger> {
        val matched = filter(items) { candidate: SeasonalParcelLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalParcelLedgers(store: SeasonalParcelLedgerStore, query: SeasonalParcelLedgerQuery): MutableList<SeasonalParcelLedger> {
    return query.apply(store.list())
}
