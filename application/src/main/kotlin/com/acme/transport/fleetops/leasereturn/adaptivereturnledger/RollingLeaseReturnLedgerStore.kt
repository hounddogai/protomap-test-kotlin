package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling lease return ledgers by code. */
interface RollingLeaseReturnLedgerStore {
    fun find(code: String): RollingLeaseReturnLedger?
    fun save(item: RollingLeaseReturnLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLeaseReturnLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLeaseReturnLedgerStore(
    var items: MutableMap<String, RollingLeaseReturnLedger>,
    var order: MutableList<String>,
) : RollingLeaseReturnLedgerStore {
    override fun find(code: String): RollingLeaseReturnLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLeaseReturnLedger) {
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

    override fun list(): MutableList<RollingLeaseReturnLedger> {
        val out = mutableListOf<RollingLeaseReturnLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLeaseReturnLedgerStore(): RollingLeaseReturnLedgerStore {
    return MemoryRollingLeaseReturnLedgerStore(
        mutableMapOf<String, RollingLeaseReturnLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLeaseReturnLedgerStore(
    var inner: RollingLeaseReturnLedgerStore,
    var cache: TtlCache<RollingLeaseReturnLedger>,
    var hits: Int,
    var misses: Int,
) : RollingLeaseReturnLedgerStore {
    override fun find(code: String): RollingLeaseReturnLedger? {
        val hit = (cache.get(code) as RollingLeaseReturnLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLeaseReturnLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLeaseReturnLedger> = inner.list()
}

fun newCachedRollingLeaseReturnLedgerStore(inner: RollingLeaseReturnLedgerStore, ttl: Int, clock: Clock): RollingLeaseReturnLedgerStore {
    return CachedRollingLeaseReturnLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingLeaseReturnLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lease return ledgers. Empty fields match all. */
data class RollingLeaseReturnLedgerQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLeaseReturnLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingLeaseReturnLedger>): MutableList<RollingLeaseReturnLedger> {
        val matched = filter(items) { candidate: RollingLeaseReturnLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLeaseReturnLedgers(store: RollingLeaseReturnLedgerStore, query: RollingLeaseReturnLedgerQuery): MutableList<RollingLeaseReturnLedger> {
    return query.apply(store.list())
}
