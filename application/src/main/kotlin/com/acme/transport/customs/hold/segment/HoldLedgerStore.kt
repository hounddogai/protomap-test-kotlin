package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold ledgers by code. */
interface HoldLedgerStore {
    fun find(code: String): HoldLedger?
    fun save(item: HoldLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldLedgerStore(
    var items: MutableMap<String, HoldLedger>,
    var order: MutableList<String>,
) : HoldLedgerStore {
    override fun find(code: String): HoldLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldLedger) {
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

    override fun list(): MutableList<HoldLedger> {
        val out = mutableListOf<HoldLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldLedgerStore(): HoldLedgerStore {
    return MemoryHoldLedgerStore(mutableMapOf<String, HoldLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldLedgerStore(
    var inner: HoldLedgerStore,
    var cache: TtlCache<HoldLedger>,
    var hits: Int,
    var misses: Int,
) : HoldLedgerStore {
    override fun find(code: String): HoldLedger? {
        val hit = (cache.get(code) as HoldLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldLedger> = inner.list()
}

fun newCachedHoldLedgerStore(inner: HoldLedgerStore, ttl: Int, clock: Clock): HoldLedgerStore {
    return CachedHoldLedgerStore(
        inner = inner,
        cache = newTtlCache<HoldLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold ledgers. Empty fields match all. */
data class HoldLedgerQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldLedger>): MutableList<HoldLedger> {
        val matched = filter(items) { candidate: HoldLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldLedgers(store: HoldLedgerStore, query: HoldLedgerQuery): MutableList<HoldLedger> {
    return query.apply(store.list())
}
