package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional sortation ledgers by code. */
interface RegionalSortationLedgerStore {
    fun find(code: String): RegionalSortationLedger?
    fun save(item: RegionalSortationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSortationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSortationLedgerStore(
    var items: MutableMap<String, RegionalSortationLedger>,
    var order: MutableList<String>,
) : RegionalSortationLedgerStore {
    override fun find(code: String): RegionalSortationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSortationLedger) {
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

    override fun list(): MutableList<RegionalSortationLedger> {
        val out = mutableListOf<RegionalSortationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSortationLedgerStore(): RegionalSortationLedgerStore {
    return MemoryRegionalSortationLedgerStore(
        mutableMapOf<String, RegionalSortationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSortationLedgerStore(
    var inner: RegionalSortationLedgerStore,
    var cache: TtlCache<RegionalSortationLedger>,
    var hits: Int,
    var misses: Int,
) : RegionalSortationLedgerStore {
    override fun find(code: String): RegionalSortationLedger? {
        val hit = (cache.get(code) as RegionalSortationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSortationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSortationLedger> = inner.list()
}

fun newCachedRegionalSortationLedgerStore(inner: RegionalSortationLedgerStore, ttl: Int, clock: Clock): RegionalSortationLedgerStore {
    return CachedRegionalSortationLedgerStore(
        inner = inner,
        cache = newTtlCache<RegionalSortationLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional sortation ledgers. Empty fields match all. */
data class RegionalSortationLedgerQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSortationLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalSortationLedger>): MutableList<RegionalSortationLedger> {
        val matched = filter(items) { candidate: RegionalSortationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSortationLedgers(store: RegionalSortationLedgerStore, query: RegionalSortationLedgerQuery): MutableList<RegionalSortationLedger> {
    return query.apply(store.list())
}
