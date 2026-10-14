package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal attempt ledgers by code. */
interface SeasonalAttemptLedgerStore {
    fun find(code: String): SeasonalAttemptLedger?
    fun save(item: SeasonalAttemptLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAttemptLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAttemptLedgerStore(
    var items: MutableMap<String, SeasonalAttemptLedger>,
    var order: MutableList<String>,
) : SeasonalAttemptLedgerStore {
    override fun find(code: String): SeasonalAttemptLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAttemptLedger) {
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

    override fun list(): MutableList<SeasonalAttemptLedger> {
        val out = mutableListOf<SeasonalAttemptLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAttemptLedgerStore(): SeasonalAttemptLedgerStore {
    return MemorySeasonalAttemptLedgerStore(
        mutableMapOf<String, SeasonalAttemptLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAttemptLedgerStore(
    var inner: SeasonalAttemptLedgerStore,
    var cache: TtlCache<SeasonalAttemptLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalAttemptLedgerStore {
    override fun find(code: String): SeasonalAttemptLedger? {
        val hit = (cache.get(code) as SeasonalAttemptLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAttemptLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAttemptLedger> = inner.list()
}

fun newCachedSeasonalAttemptLedgerStore(inner: SeasonalAttemptLedgerStore, ttl: Int, clock: Clock): SeasonalAttemptLedgerStore {
    return CachedSeasonalAttemptLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalAttemptLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal attempt ledgers. Empty fields match all. */
data class SeasonalAttemptLedgerQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAttemptLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAttemptLedger>): MutableList<SeasonalAttemptLedger> {
        val matched = filter(items) { candidate: SeasonalAttemptLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAttemptLedgers(store: SeasonalAttemptLedgerStore, query: SeasonalAttemptLedgerQuery): MutableList<SeasonalAttemptLedger> {
    return query.apply(store.list())
}
