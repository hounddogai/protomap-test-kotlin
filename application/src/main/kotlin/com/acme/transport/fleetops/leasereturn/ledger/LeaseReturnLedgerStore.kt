package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return ledgers by code. */
interface LeaseReturnLedgerStore {
    fun find(code: String): LeaseReturnLedger?
    fun save(item: LeaseReturnLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnLedgerStore(
    var items: MutableMap<String, LeaseReturnLedger>,
    var order: MutableList<String>,
) : LeaseReturnLedgerStore {
    override fun find(code: String): LeaseReturnLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnLedger) {
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

    override fun list(): MutableList<LeaseReturnLedger> {
        val out = mutableListOf<LeaseReturnLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnLedgerStore(): LeaseReturnLedgerStore {
    return MemoryLeaseReturnLedgerStore(mutableMapOf<String, LeaseReturnLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnLedgerStore(
    var inner: LeaseReturnLedgerStore,
    var cache: TtlCache<LeaseReturnLedger>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnLedgerStore {
    override fun find(code: String): LeaseReturnLedger? {
        val hit = (cache.get(code) as LeaseReturnLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnLedger> = inner.list()
}

fun newCachedLeaseReturnLedgerStore(inner: LeaseReturnLedgerStore, ttl: Int, clock: Clock): LeaseReturnLedgerStore {
    return CachedLeaseReturnLedgerStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return ledgers. Empty fields match all. */
data class LeaseReturnLedgerQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnLedger>): MutableList<LeaseReturnLedger> {
        val matched = filter(items) { candidate: LeaseReturnLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnLedgers(store: LeaseReturnLedgerStore, query: LeaseReturnLedgerQuery): MutableList<LeaseReturnLedger> {
    return query.apply(store.list())
}
