package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance ledgers by code. */
interface ClearanceLedgerStore {
    fun find(code: String): ClearanceLedger?
    fun save(item: ClearanceLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceLedgerStore(
    var items: MutableMap<String, ClearanceLedger>,
    var order: MutableList<String>,
) : ClearanceLedgerStore {
    override fun find(code: String): ClearanceLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceLedger) {
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

    override fun list(): MutableList<ClearanceLedger> {
        val out = mutableListOf<ClearanceLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceLedgerStore(): ClearanceLedgerStore {
    return MemoryClearanceLedgerStore(mutableMapOf<String, ClearanceLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceLedgerStore(
    var inner: ClearanceLedgerStore,
    var cache: TtlCache<ClearanceLedger>,
    var hits: Int,
    var misses: Int,
) : ClearanceLedgerStore {
    override fun find(code: String): ClearanceLedger? {
        val hit = (cache.get(code) as ClearanceLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceLedger> = inner.list()
}

fun newCachedClearanceLedgerStore(inner: ClearanceLedgerStore, ttl: Int, clock: Clock): ClearanceLedgerStore {
    return CachedClearanceLedgerStore(
        inner = inner,
        cache = newTtlCache<ClearanceLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance ledgers. Empty fields match all. */
data class ClearanceLedgerQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceLedger>): MutableList<ClearanceLedger> {
        val matched = filter(items) { candidate: ClearanceLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceLedgers(store: ClearanceLedgerStore, query: ClearanceLedgerQuery): MutableList<ClearanceLedger> {
    return query.apply(store.list())
}
