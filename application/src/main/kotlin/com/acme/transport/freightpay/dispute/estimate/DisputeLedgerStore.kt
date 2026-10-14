package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute ledgers by code. */
interface DisputeLedgerStore {
    fun find(code: String): DisputeLedger?
    fun save(item: DisputeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeLedgerStore(
    var items: MutableMap<String, DisputeLedger>,
    var order: MutableList<String>,
) : DisputeLedgerStore {
    override fun find(code: String): DisputeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeLedger) {
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

    override fun list(): MutableList<DisputeLedger> {
        val out = mutableListOf<DisputeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeLedgerStore(): DisputeLedgerStore {
    return MemoryDisputeLedgerStore(mutableMapOf<String, DisputeLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeLedgerStore(
    var inner: DisputeLedgerStore,
    var cache: TtlCache<DisputeLedger>,
    var hits: Int,
    var misses: Int,
) : DisputeLedgerStore {
    override fun find(code: String): DisputeLedger? {
        val hit = (cache.get(code) as DisputeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeLedger> = inner.list()
}

fun newCachedDisputeLedgerStore(inner: DisputeLedgerStore, ttl: Int, clock: Clock): DisputeLedgerStore {
    return CachedDisputeLedgerStore(
        inner = inner,
        cache = newTtlCache<DisputeLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute ledgers. Empty fields match all. */
data class DisputeLedgerQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DisputeLedger>): MutableList<DisputeLedger> {
        val matched = filter(items) { candidate: DisputeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeLedgers(store: DisputeLedgerStore, query: DisputeLedgerQuery): MutableList<DisputeLedger> {
    return query.apply(store.list())
}
