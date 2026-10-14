package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal hub ledgers by code. */
interface SeasonalHubLedgerStore {
    fun find(code: String): SeasonalHubLedger?
    fun save(item: SeasonalHubLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalHubLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalHubLedgerStore(
    var items: MutableMap<String, SeasonalHubLedger>,
    var order: MutableList<String>,
) : SeasonalHubLedgerStore {
    override fun find(code: String): SeasonalHubLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalHubLedger) {
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

    override fun list(): MutableList<SeasonalHubLedger> {
        val out = mutableListOf<SeasonalHubLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalHubLedgerStore(): SeasonalHubLedgerStore {
    return MemorySeasonalHubLedgerStore(mutableMapOf<String, SeasonalHubLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalHubLedgerStore(
    var inner: SeasonalHubLedgerStore,
    var cache: TtlCache<SeasonalHubLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalHubLedgerStore {
    override fun find(code: String): SeasonalHubLedger? {
        val hit = (cache.get(code) as SeasonalHubLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalHubLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalHubLedger> = inner.list()
}

fun newCachedSeasonalHubLedgerStore(inner: SeasonalHubLedgerStore, ttl: Int, clock: Clock): SeasonalHubLedgerStore {
    return CachedSeasonalHubLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalHubLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal hub ledgers. Empty fields match all. */
data class SeasonalHubLedgerQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalHubLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalHubLedger>): MutableList<SeasonalHubLedger> {
        val matched = filter(items) { candidate: SeasonalHubLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalHubLedgers(store: SeasonalHubLedgerStore, query: SeasonalHubLedgerQuery): MutableList<SeasonalHubLedger> {
    return query.apply(store.list())
}
