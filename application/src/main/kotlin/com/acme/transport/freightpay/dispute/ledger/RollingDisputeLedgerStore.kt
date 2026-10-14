package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute ledgers by code. */
interface RollingDisputeLedgerStore {
    fun find(code: String): RollingDisputeLedger?
    fun save(item: RollingDisputeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputeLedgerStore(
    var items: MutableMap<String, RollingDisputeLedger>,
    var order: MutableList<String>,
) : RollingDisputeLedgerStore {
    override fun find(code: String): RollingDisputeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputeLedger) {
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

    override fun list(): MutableList<RollingDisputeLedger> {
        val out = mutableListOf<RollingDisputeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputeLedgerStore(): RollingDisputeLedgerStore {
    return MemoryRollingDisputeLedgerStore(
        mutableMapOf<String, RollingDisputeLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputeLedgerStore(
    var inner: RollingDisputeLedgerStore,
    var cache: TtlCache<RollingDisputeLedger>,
    var hits: Int,
    var misses: Int,
) : RollingDisputeLedgerStore {
    override fun find(code: String): RollingDisputeLedger? {
        val hit = (cache.get(code) as RollingDisputeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputeLedger> = inner.list()
}

fun newCachedRollingDisputeLedgerStore(inner: RollingDisputeLedgerStore, ttl: Int, clock: Clock): RollingDisputeLedgerStore {
    return CachedRollingDisputeLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingDisputeLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute ledgers. Empty fields match all. */
data class RollingDisputeLedgerQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputeLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDisputeLedger>): MutableList<RollingDisputeLedger> {
        val matched = filter(items) { candidate: RollingDisputeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputeLedgers(store: RollingDisputeLedgerStore, query: RollingDisputeLedgerQuery): MutableList<RollingDisputeLedger> {
    return query.apply(store.list())
}
