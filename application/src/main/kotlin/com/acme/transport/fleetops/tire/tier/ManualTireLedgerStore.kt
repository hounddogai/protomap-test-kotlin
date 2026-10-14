package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tire ledgers by code. */
interface ManualTireLedgerStore {
    fun find(code: String): ManualTireLedger?
    fun save(item: ManualTireLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTireLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTireLedgerStore(
    var items: MutableMap<String, ManualTireLedger>,
    var order: MutableList<String>,
) : ManualTireLedgerStore {
    override fun find(code: String): ManualTireLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTireLedger) {
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

    override fun list(): MutableList<ManualTireLedger> {
        val out = mutableListOf<ManualTireLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTireLedgerStore(): ManualTireLedgerStore {
    return MemoryManualTireLedgerStore(mutableMapOf<String, ManualTireLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTireLedgerStore(
    var inner: ManualTireLedgerStore,
    var cache: TtlCache<ManualTireLedger>,
    var hits: Int,
    var misses: Int,
) : ManualTireLedgerStore {
    override fun find(code: String): ManualTireLedger? {
        val hit = (cache.get(code) as ManualTireLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTireLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTireLedger> = inner.list()
}

fun newCachedManualTireLedgerStore(inner: ManualTireLedgerStore, ttl: Int, clock: Clock): ManualTireLedgerStore {
    return CachedManualTireLedgerStore(
        inner = inner,
        cache = newTtlCache<ManualTireLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tire ledgers. Empty fields match all. */
data class ManualTireLedgerQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTireLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ManualTireLedger>): MutableList<ManualTireLedger> {
        val matched = filter(items) { candidate: ManualTireLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTireLedgers(store: ManualTireLedgerStore, query: ManualTireLedgerQuery): MutableList<ManualTireLedger> {
    return query.apply(store.list())
}
