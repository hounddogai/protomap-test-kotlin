package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery ledgers by code. */
interface SeasonalRedeliveryLedgerStore {
    fun find(code: String): SeasonalRedeliveryLedger?
    fun save(item: SeasonalRedeliveryLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliveryLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryLedgerStore(
    var items: MutableMap<String, SeasonalRedeliveryLedger>,
    var order: MutableList<String>,
) : SeasonalRedeliveryLedgerStore {
    override fun find(code: String): SeasonalRedeliveryLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliveryLedger) {
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

    override fun list(): MutableList<SeasonalRedeliveryLedger> {
        val out = mutableListOf<SeasonalRedeliveryLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryLedgerStore(): SeasonalRedeliveryLedgerStore {
    return MemorySeasonalRedeliveryLedgerStore(
        mutableMapOf<String, SeasonalRedeliveryLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryLedgerStore(
    var inner: SeasonalRedeliveryLedgerStore,
    var cache: TtlCache<SeasonalRedeliveryLedger>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryLedgerStore {
    override fun find(code: String): SeasonalRedeliveryLedger? {
        val hit = (cache.get(code) as SeasonalRedeliveryLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliveryLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliveryLedger> = inner.list()
}

fun newCachedSeasonalRedeliveryLedgerStore(inner: SeasonalRedeliveryLedgerStore, ttl: Int, clock: Clock): SeasonalRedeliveryLedgerStore {
    return CachedSeasonalRedeliveryLedgerStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliveryLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery ledgers. Empty fields match all. */
data class SeasonalRedeliveryLedgerQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliveryLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliveryLedger>): MutableList<SeasonalRedeliveryLedger> {
        val matched = filter(items) { candidate: SeasonalRedeliveryLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliveryLedgers(store: SeasonalRedeliveryLedgerStore, query: SeasonalRedeliveryLedgerQuery): MutableList<SeasonalRedeliveryLedger> {
    return query.apply(store.list())
}
