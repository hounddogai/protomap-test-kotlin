package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door ledgers by code. */
interface DockDoorLedgerStore {
    fun find(code: String): DockDoorLedger?
    fun save(item: DockDoorLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorLedgerStore(
    var items: MutableMap<String, DockDoorLedger>,
    var order: MutableList<String>,
) : DockDoorLedgerStore {
    override fun find(code: String): DockDoorLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorLedger) {
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

    override fun list(): MutableList<DockDoorLedger> {
        val out = mutableListOf<DockDoorLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorLedgerStore(): DockDoorLedgerStore {
    return MemoryDockDoorLedgerStore(mutableMapOf<String, DockDoorLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorLedgerStore(
    var inner: DockDoorLedgerStore,
    var cache: TtlCache<DockDoorLedger>,
    var hits: Int,
    var misses: Int,
) : DockDoorLedgerStore {
    override fun find(code: String): DockDoorLedger? {
        val hit = (cache.get(code) as DockDoorLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorLedger> = inner.list()
}

fun newCachedDockDoorLedgerStore(inner: DockDoorLedgerStore, ttl: Int, clock: Clock): DockDoorLedgerStore {
    return CachedDockDoorLedgerStore(
        inner = inner,
        cache = newTtlCache<DockDoorLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door ledgers. Empty fields match all. */
data class DockDoorLedgerQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorLedger>): MutableList<DockDoorLedger> {
        val matched = filter(items) { candidate: DockDoorLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorLedgers(store: DockDoorLedgerStore, query: DockDoorLedgerQuery): MutableList<DockDoorLedger> {
    return query.apply(store.list())
}
