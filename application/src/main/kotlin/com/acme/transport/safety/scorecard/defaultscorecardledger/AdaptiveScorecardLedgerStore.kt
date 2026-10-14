package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive scorecard ledgers by code. */
interface AdaptiveScorecardLedgerStore {
    fun find(code: String): AdaptiveScorecardLedger?
    fun save(item: AdaptiveScorecardLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveScorecardLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveScorecardLedgerStore(
    var items: MutableMap<String, AdaptiveScorecardLedger>,
    var order: MutableList<String>,
) : AdaptiveScorecardLedgerStore {
    override fun find(code: String): AdaptiveScorecardLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveScorecardLedger) {
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

    override fun list(): MutableList<AdaptiveScorecardLedger> {
        val out = mutableListOf<AdaptiveScorecardLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveScorecardLedgerStore(): AdaptiveScorecardLedgerStore {
    return MemoryAdaptiveScorecardLedgerStore(
        mutableMapOf<String, AdaptiveScorecardLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveScorecardLedgerStore(
    var inner: AdaptiveScorecardLedgerStore,
    var cache: TtlCache<AdaptiveScorecardLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveScorecardLedgerStore {
    override fun find(code: String): AdaptiveScorecardLedger? {
        val hit = (cache.get(code) as AdaptiveScorecardLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveScorecardLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveScorecardLedger> = inner.list()
}

fun newCachedAdaptiveScorecardLedgerStore(inner: AdaptiveScorecardLedgerStore, ttl: Int, clock: Clock): AdaptiveScorecardLedgerStore {
    return CachedAdaptiveScorecardLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveScorecardLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive scorecard ledgers. Empty fields match all. */
data class AdaptiveScorecardLedgerQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveScorecardLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveScorecardLedger>): MutableList<AdaptiveScorecardLedger> {
        val matched = filter(items) { candidate: AdaptiveScorecardLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveScorecardLedgers(store: AdaptiveScorecardLedgerStore, query: AdaptiveScorecardLedgerQuery): MutableList<AdaptiveScorecardLedger> {
    return query.apply(store.list())
}
