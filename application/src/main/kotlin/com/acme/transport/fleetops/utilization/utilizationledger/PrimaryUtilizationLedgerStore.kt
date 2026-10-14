package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary utilization ledgers by code. */
interface PrimaryUtilizationLedgerStore {
    fun find(code: String): PrimaryUtilizationLedger?
    fun save(item: PrimaryUtilizationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryUtilizationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryUtilizationLedgerStore(
    var items: MutableMap<String, PrimaryUtilizationLedger>,
    var order: MutableList<String>,
) : PrimaryUtilizationLedgerStore {
    override fun find(code: String): PrimaryUtilizationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryUtilizationLedger) {
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

    override fun list(): MutableList<PrimaryUtilizationLedger> {
        val out = mutableListOf<PrimaryUtilizationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryUtilizationLedgerStore(): PrimaryUtilizationLedgerStore {
    return MemoryPrimaryUtilizationLedgerStore(
        mutableMapOf<String, PrimaryUtilizationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryUtilizationLedgerStore(
    var inner: PrimaryUtilizationLedgerStore,
    var cache: TtlCache<PrimaryUtilizationLedger>,
    var hits: Int,
    var misses: Int,
) : PrimaryUtilizationLedgerStore {
    override fun find(code: String): PrimaryUtilizationLedger? {
        val hit = (cache.get(code) as PrimaryUtilizationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryUtilizationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryUtilizationLedger> = inner.list()
}

fun newCachedPrimaryUtilizationLedgerStore(inner: PrimaryUtilizationLedgerStore, ttl: Int, clock: Clock): PrimaryUtilizationLedgerStore {
    return CachedPrimaryUtilizationLedgerStore(
        inner = inner,
        cache = newTtlCache<PrimaryUtilizationLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary utilization ledgers. Empty fields match all. */
data class PrimaryUtilizationLedgerQuery(
    var channel: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryUtilizationLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryUtilizationLedger>): MutableList<PrimaryUtilizationLedger> {
        val matched = filter(items) { candidate: PrimaryUtilizationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryUtilizationLedgers(store: PrimaryUtilizationLedgerStore, query: PrimaryUtilizationLedgerQuery): MutableList<PrimaryUtilizationLedger> {
    return query.apply(store.list())
}
