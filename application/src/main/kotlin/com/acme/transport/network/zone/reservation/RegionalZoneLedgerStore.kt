package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional zone ledgers by code. */
interface RegionalZoneLedgerStore {
    fun find(code: String): RegionalZoneLedger?
    fun save(item: RegionalZoneLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalZoneLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalZoneLedgerStore(
    var items: MutableMap<String, RegionalZoneLedger>,
    var order: MutableList<String>,
) : RegionalZoneLedgerStore {
    override fun find(code: String): RegionalZoneLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalZoneLedger) {
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

    override fun list(): MutableList<RegionalZoneLedger> {
        val out = mutableListOf<RegionalZoneLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalZoneLedgerStore(): RegionalZoneLedgerStore {
    return MemoryRegionalZoneLedgerStore(mutableMapOf<String, RegionalZoneLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalZoneLedgerStore(
    var inner: RegionalZoneLedgerStore,
    var cache: TtlCache<RegionalZoneLedger>,
    var hits: Int,
    var misses: Int,
) : RegionalZoneLedgerStore {
    override fun find(code: String): RegionalZoneLedger? {
        val hit = (cache.get(code) as RegionalZoneLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalZoneLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalZoneLedger> = inner.list()
}

fun newCachedRegionalZoneLedgerStore(inner: RegionalZoneLedgerStore, ttl: Int, clock: Clock): RegionalZoneLedgerStore {
    return CachedRegionalZoneLedgerStore(
        inner = inner,
        cache = newTtlCache<RegionalZoneLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional zone ledgers. Empty fields match all. */
data class RegionalZoneLedgerQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalZoneLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RegionalZoneLedger>): MutableList<RegionalZoneLedger> {
        val matched = filter(items) { candidate: RegionalZoneLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalZoneLedgers(store: RegionalZoneLedgerStore, query: RegionalZoneLedgerQuery): MutableList<RegionalZoneLedger> {
    return query.apply(store.list())
}
