package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period ledgers by code. */
interface DutyPeriodLedgerStore {
    fun find(code: String): DutyPeriodLedger?
    fun save(item: DutyPeriodLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodLedgerStore(
    var items: MutableMap<String, DutyPeriodLedger>,
    var order: MutableList<String>,
) : DutyPeriodLedgerStore {
    override fun find(code: String): DutyPeriodLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodLedger) {
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

    override fun list(): MutableList<DutyPeriodLedger> {
        val out = mutableListOf<DutyPeriodLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodLedgerStore(): DutyPeriodLedgerStore {
    return MemoryDutyPeriodLedgerStore(mutableMapOf<String, DutyPeriodLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodLedgerStore(
    var inner: DutyPeriodLedgerStore,
    var cache: TtlCache<DutyPeriodLedger>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodLedgerStore {
    override fun find(code: String): DutyPeriodLedger? {
        val hit = (cache.get(code) as DutyPeriodLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodLedger> = inner.list()
}

fun newCachedDutyPeriodLedgerStore(inner: DutyPeriodLedgerStore, ttl: Int, clock: Clock): DutyPeriodLedgerStore {
    return CachedDutyPeriodLedgerStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period ledgers. Empty fields match all. */
data class DutyPeriodLedgerQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodLedger>): MutableList<DutyPeriodLedger> {
        val matched = filter(items) { candidate: DutyPeriodLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodLedgers(store: DutyPeriodLedgerStore, query: DutyPeriodLedgerQuery): MutableList<DutyPeriodLedger> {
    return query.apply(store.list())
}
