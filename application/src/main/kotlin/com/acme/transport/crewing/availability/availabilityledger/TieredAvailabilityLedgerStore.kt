package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered availability ledgers by code. */
interface TieredAvailabilityLedgerStore {
    fun find(code: String): TieredAvailabilityLedger?
    fun save(item: TieredAvailabilityLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAvailabilityLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAvailabilityLedgerStore(
    var items: MutableMap<String, TieredAvailabilityLedger>,
    var order: MutableList<String>,
) : TieredAvailabilityLedgerStore {
    override fun find(code: String): TieredAvailabilityLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAvailabilityLedger) {
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

    override fun list(): MutableList<TieredAvailabilityLedger> {
        val out = mutableListOf<TieredAvailabilityLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAvailabilityLedgerStore(): TieredAvailabilityLedgerStore {
    return MemoryTieredAvailabilityLedgerStore(
        mutableMapOf<String, TieredAvailabilityLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAvailabilityLedgerStore(
    var inner: TieredAvailabilityLedgerStore,
    var cache: TtlCache<TieredAvailabilityLedger>,
    var hits: Int,
    var misses: Int,
) : TieredAvailabilityLedgerStore {
    override fun find(code: String): TieredAvailabilityLedger? {
        val hit = (cache.get(code) as TieredAvailabilityLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAvailabilityLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAvailabilityLedger> = inner.list()
}

fun newCachedTieredAvailabilityLedgerStore(inner: TieredAvailabilityLedgerStore, ttl: Int, clock: Clock): TieredAvailabilityLedgerStore {
    return CachedTieredAvailabilityLedgerStore(
        inner = inner,
        cache = newTtlCache<TieredAvailabilityLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered availability ledgers. Empty fields match all. */
data class TieredAvailabilityLedgerQuery(
    var segment: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAvailabilityLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAvailabilityLedger>): MutableList<TieredAvailabilityLedger> {
        val matched = filter(items) { candidate: TieredAvailabilityLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAvailabilityLedgers(store: TieredAvailabilityLedgerStore, query: TieredAvailabilityLedgerQuery): MutableList<TieredAvailabilityLedger> {
    return query.apply(store.list())
}
