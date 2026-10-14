package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global redelivery ledgers by code. */
interface GlobalRedeliveryLedgerStore {
    fun find(code: String): GlobalRedeliveryLedger?
    fun save(item: GlobalRedeliveryLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRedeliveryLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRedeliveryLedgerStore(
    var items: MutableMap<String, GlobalRedeliveryLedger>,
    var order: MutableList<String>,
) : GlobalRedeliveryLedgerStore {
    override fun find(code: String): GlobalRedeliveryLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRedeliveryLedger) {
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

    override fun list(): MutableList<GlobalRedeliveryLedger> {
        val out = mutableListOf<GlobalRedeliveryLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRedeliveryLedgerStore(): GlobalRedeliveryLedgerStore {
    return MemoryGlobalRedeliveryLedgerStore(
        mutableMapOf<String, GlobalRedeliveryLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRedeliveryLedgerStore(
    var inner: GlobalRedeliveryLedgerStore,
    var cache: TtlCache<GlobalRedeliveryLedger>,
    var hits: Int,
    var misses: Int,
) : GlobalRedeliveryLedgerStore {
    override fun find(code: String): GlobalRedeliveryLedger? {
        val hit = (cache.get(code) as GlobalRedeliveryLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRedeliveryLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRedeliveryLedger> = inner.list()
}

fun newCachedGlobalRedeliveryLedgerStore(inner: GlobalRedeliveryLedgerStore, ttl: Int, clock: Clock): GlobalRedeliveryLedgerStore {
    return CachedGlobalRedeliveryLedgerStore(
        inner = inner,
        cache = newTtlCache<GlobalRedeliveryLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global redelivery ledgers. Empty fields match all. */
data class GlobalRedeliveryLedgerQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRedeliveryLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRedeliveryLedger>): MutableList<GlobalRedeliveryLedger> {
        val matched = filter(items) { candidate: GlobalRedeliveryLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRedeliveryLedgers(store: GlobalRedeliveryLedgerStore, query: GlobalRedeliveryLedgerQuery): MutableList<GlobalRedeliveryLedger> {
    return query.apply(store.list())
}
