package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity ledgers by code. */
interface CapacityLedgerStore {
    fun find(code: String): CapacityLedger?
    fun save(item: CapacityLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityLedgerStore(
    var items: MutableMap<String, CapacityLedger>,
    var order: MutableList<String>,
) : CapacityLedgerStore {
    override fun find(code: String): CapacityLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityLedger) {
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

    override fun list(): MutableList<CapacityLedger> {
        val out = mutableListOf<CapacityLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityLedgerStore(): CapacityLedgerStore {
    return MemoryCapacityLedgerStore(mutableMapOf<String, CapacityLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityLedgerStore(
    var inner: CapacityLedgerStore,
    var cache: TtlCache<CapacityLedger>,
    var hits: Int,
    var misses: Int,
) : CapacityLedgerStore {
    override fun find(code: String): CapacityLedger? {
        val hit = (cache.get(code) as CapacityLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityLedger> = inner.list()
}

fun newCachedCapacityLedgerStore(inner: CapacityLedgerStore, ttl: Int, clock: Clock): CapacityLedgerStore {
    return CachedCapacityLedgerStore(
        inner = inner,
        cache = newTtlCache<CapacityLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity ledgers. Empty fields match all. */
data class CapacityLedgerQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityLedger>): MutableList<CapacityLedger> {
        val matched = filter(items) { candidate: CapacityLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityLedgers(store: CapacityLedgerStore, query: CapacityLedgerQuery): MutableList<CapacityLedger> {
    return query.apply(store.list())
}
