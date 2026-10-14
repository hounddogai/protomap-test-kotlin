package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection ledgers by code. */
interface LegacyInspectionLedgerStore {
    fun find(code: String): LegacyInspectionLedger?
    fun save(item: LegacyInspectionLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionLedgerStore(
    var items: MutableMap<String, LegacyInspectionLedger>,
    var order: MutableList<String>,
) : LegacyInspectionLedgerStore {
    override fun find(code: String): LegacyInspectionLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionLedger) {
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

    override fun list(): MutableList<LegacyInspectionLedger> {
        val out = mutableListOf<LegacyInspectionLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionLedgerStore(): LegacyInspectionLedgerStore {
    return MemoryLegacyInspectionLedgerStore(
        mutableMapOf<String, LegacyInspectionLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionLedgerStore(
    var inner: LegacyInspectionLedgerStore,
    var cache: TtlCache<LegacyInspectionLedger>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionLedgerStore {
    override fun find(code: String): LegacyInspectionLedger? {
        val hit = (cache.get(code) as LegacyInspectionLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionLedger> = inner.list()
}

fun newCachedLegacyInspectionLedgerStore(inner: LegacyInspectionLedgerStore, ttl: Int, clock: Clock): LegacyInspectionLedgerStore {
    return CachedLegacyInspectionLedgerStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection ledgers. Empty fields match all. */
data class LegacyInspectionLedgerQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionLedger>): MutableList<LegacyInspectionLedger> {
        val matched = filter(items) { candidate: LegacyInspectionLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionLedgers(store: LegacyInspectionLedgerStore, query: LegacyInspectionLedgerQuery): MutableList<LegacyInspectionLedger> {
    return query.apply(store.list())
}
