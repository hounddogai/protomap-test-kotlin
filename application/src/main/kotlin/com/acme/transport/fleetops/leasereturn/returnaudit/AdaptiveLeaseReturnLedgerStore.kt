package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive lease return ledgers by code. */
interface AdaptiveLeaseReturnLedgerStore {
    fun find(code: String): AdaptiveLeaseReturnLedger?
    fun save(item: AdaptiveLeaseReturnLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLeaseReturnLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLeaseReturnLedgerStore(
    var items: MutableMap<String, AdaptiveLeaseReturnLedger>,
    var order: MutableList<String>,
) : AdaptiveLeaseReturnLedgerStore {
    override fun find(code: String): AdaptiveLeaseReturnLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLeaseReturnLedger) {
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

    override fun list(): MutableList<AdaptiveLeaseReturnLedger> {
        val out = mutableListOf<AdaptiveLeaseReturnLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLeaseReturnLedgerStore(): AdaptiveLeaseReturnLedgerStore {
    return MemoryAdaptiveLeaseReturnLedgerStore(
        mutableMapOf<String, AdaptiveLeaseReturnLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLeaseReturnLedgerStore(
    var inner: AdaptiveLeaseReturnLedgerStore,
    var cache: TtlCache<AdaptiveLeaseReturnLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLeaseReturnLedgerStore {
    override fun find(code: String): AdaptiveLeaseReturnLedger? {
        val hit = (cache.get(code) as AdaptiveLeaseReturnLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLeaseReturnLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLeaseReturnLedger> = inner.list()
}

fun newCachedAdaptiveLeaseReturnLedgerStore(inner: AdaptiveLeaseReturnLedgerStore, ttl: Int, clock: Clock): AdaptiveLeaseReturnLedgerStore {
    return CachedAdaptiveLeaseReturnLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLeaseReturnLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lease return ledgers. Empty fields match all. */
data class AdaptiveLeaseReturnLedgerQuery(
    var category: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLeaseReturnLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLeaseReturnLedger>): MutableList<AdaptiveLeaseReturnLedger> {
        val matched = filter(items) { candidate: AdaptiveLeaseReturnLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLeaseReturnLedgers(store: AdaptiveLeaseReturnLedgerStore, query: AdaptiveLeaseReturnLedgerQuery): MutableList<AdaptiveLeaseReturnLedger> {
    return query.apply(store.list())
}
