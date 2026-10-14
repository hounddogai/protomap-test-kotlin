package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal rest break ledgers by code. */
interface SeasonalRestBreakLedgerStore {
    fun find(code: String): SeasonalRestBreakLedger?
    fun save(item: SeasonalRestBreakLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRestBreakLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRestBreakLedgerStore(
    var items: MutableMap<String, SeasonalRestBreakLedger>,
    var order: MutableList<String>,
) : SeasonalRestBreakLedgerStore {
    override fun find(code: String): SeasonalRestBreakLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRestBreakLedger) {
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

    override fun list(): MutableList<SeasonalRestBreakLedger> {
        val out = mutableListOf<SeasonalRestBreakLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRestBreakLedgerStore(): SeasonalRestBreakLedgerStore {
    return MemorySeasonalRestBreakLedgerStore(
        mutableMapOf<String, SeasonalRestBreakLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRestBreakLedgerStore(
    var inner: SeasonalRestBreakLedgerStore,
    var cache: TtlCache<SeasonalRestBreakLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalRestBreakLedgerStore {
    override fun find(code: String): SeasonalRestBreakLedger? {
        val hit = (cache.get(code) as SeasonalRestBreakLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRestBreakLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRestBreakLedger> = inner.list()
}

fun newCachedSeasonalRestBreakLedgerStore(inner: SeasonalRestBreakLedgerStore, ttl: Int, clock: Clock): SeasonalRestBreakLedgerStore {
    return CachedSeasonalRestBreakLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalRestBreakLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal rest break ledgers. Empty fields match all. */
data class SeasonalRestBreakLedgerQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRestBreakLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRestBreakLedger>): MutableList<SeasonalRestBreakLedger> {
        val matched = filter(items) { candidate: SeasonalRestBreakLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRestBreakLedgers(store: SeasonalRestBreakLedgerStore, query: SeasonalRestBreakLedgerQuery): MutableList<SeasonalRestBreakLedger> {
    return query.apply(store.list())
}
