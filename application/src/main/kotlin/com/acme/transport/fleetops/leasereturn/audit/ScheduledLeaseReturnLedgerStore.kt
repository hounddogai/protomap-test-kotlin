package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled lease return ledgers by code. */
interface ScheduledLeaseReturnLedgerStore {
    fun find(code: String): ScheduledLeaseReturnLedger?
    fun save(item: ScheduledLeaseReturnLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLeaseReturnLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLeaseReturnLedgerStore(
    var items: MutableMap<String, ScheduledLeaseReturnLedger>,
    var order: MutableList<String>,
) : ScheduledLeaseReturnLedgerStore {
    override fun find(code: String): ScheduledLeaseReturnLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLeaseReturnLedger) {
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

    override fun list(): MutableList<ScheduledLeaseReturnLedger> {
        val out = mutableListOf<ScheduledLeaseReturnLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLeaseReturnLedgerStore(): ScheduledLeaseReturnLedgerStore {
    return MemoryScheduledLeaseReturnLedgerStore(
        mutableMapOf<String, ScheduledLeaseReturnLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLeaseReturnLedgerStore(
    var inner: ScheduledLeaseReturnLedgerStore,
    var cache: TtlCache<ScheduledLeaseReturnLedger>,
    var hits: Int,
    var misses: Int,
) : ScheduledLeaseReturnLedgerStore {
    override fun find(code: String): ScheduledLeaseReturnLedger? {
        val hit = (cache.get(code) as ScheduledLeaseReturnLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLeaseReturnLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLeaseReturnLedger> = inner.list()
}

fun newCachedScheduledLeaseReturnLedgerStore(inner: ScheduledLeaseReturnLedgerStore, ttl: Int, clock: Clock): ScheduledLeaseReturnLedgerStore {
    return CachedScheduledLeaseReturnLedgerStore(
        inner = inner,
        cache = newTtlCache<ScheduledLeaseReturnLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lease return ledgers. Empty fields match all. */
data class ScheduledLeaseReturnLedgerQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLeaseReturnLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLeaseReturnLedger>): MutableList<ScheduledLeaseReturnLedger> {
        val matched = filter(items) { candidate: ScheduledLeaseReturnLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLeaseReturnLedgers(store: ScheduledLeaseReturnLedgerStore, query: ScheduledLeaseReturnLedgerQuery): MutableList<ScheduledLeaseReturnLedger> {
    return query.apply(store.list())
}
