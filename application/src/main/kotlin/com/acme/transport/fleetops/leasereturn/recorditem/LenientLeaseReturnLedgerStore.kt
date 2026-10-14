package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient lease return ledgers by code. */
interface LenientLeaseReturnLedgerStore {
    fun find(code: String): LenientLeaseReturnLedger?
    fun save(item: LenientLeaseReturnLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLeaseReturnLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLeaseReturnLedgerStore(
    var items: MutableMap<String, LenientLeaseReturnLedger>,
    var order: MutableList<String>,
) : LenientLeaseReturnLedgerStore {
    override fun find(code: String): LenientLeaseReturnLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLeaseReturnLedger) {
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

    override fun list(): MutableList<LenientLeaseReturnLedger> {
        val out = mutableListOf<LenientLeaseReturnLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLeaseReturnLedgerStore(): LenientLeaseReturnLedgerStore {
    return MemoryLenientLeaseReturnLedgerStore(
        mutableMapOf<String, LenientLeaseReturnLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLeaseReturnLedgerStore(
    var inner: LenientLeaseReturnLedgerStore,
    var cache: TtlCache<LenientLeaseReturnLedger>,
    var hits: Int,
    var misses: Int,
) : LenientLeaseReturnLedgerStore {
    override fun find(code: String): LenientLeaseReturnLedger? {
        val hit = (cache.get(code) as LenientLeaseReturnLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLeaseReturnLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLeaseReturnLedger> = inner.list()
}

fun newCachedLenientLeaseReturnLedgerStore(inner: LenientLeaseReturnLedgerStore, ttl: Int, clock: Clock): LenientLeaseReturnLedgerStore {
    return CachedLenientLeaseReturnLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientLeaseReturnLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient lease return ledgers. Empty fields match all. */
data class LenientLeaseReturnLedgerQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLeaseReturnLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientLeaseReturnLedger>): MutableList<LenientLeaseReturnLedger> {
        val matched = filter(items) { candidate: LenientLeaseReturnLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLeaseReturnLedgers(store: LenientLeaseReturnLedgerStore, query: LenientLeaseReturnLedgerQuery): MutableList<LenientLeaseReturnLedger> {
    return query.apply(store.list())
}
