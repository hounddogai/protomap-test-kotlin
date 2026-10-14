package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual duty period ledgers by code. */
interface ManualDutyPeriodLedgerStore {
    fun find(code: String): ManualDutyPeriodLedger?
    fun save(item: ManualDutyPeriodLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDutyPeriodLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDutyPeriodLedgerStore(
    var items: MutableMap<String, ManualDutyPeriodLedger>,
    var order: MutableList<String>,
) : ManualDutyPeriodLedgerStore {
    override fun find(code: String): ManualDutyPeriodLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDutyPeriodLedger) {
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

    override fun list(): MutableList<ManualDutyPeriodLedger> {
        val out = mutableListOf<ManualDutyPeriodLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDutyPeriodLedgerStore(): ManualDutyPeriodLedgerStore {
    return MemoryManualDutyPeriodLedgerStore(
        mutableMapOf<String, ManualDutyPeriodLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDutyPeriodLedgerStore(
    var inner: ManualDutyPeriodLedgerStore,
    var cache: TtlCache<ManualDutyPeriodLedger>,
    var hits: Int,
    var misses: Int,
) : ManualDutyPeriodLedgerStore {
    override fun find(code: String): ManualDutyPeriodLedger? {
        val hit = (cache.get(code) as ManualDutyPeriodLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDutyPeriodLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDutyPeriodLedger> = inner.list()
}

fun newCachedManualDutyPeriodLedgerStore(inner: ManualDutyPeriodLedgerStore, ttl: Int, clock: Clock): ManualDutyPeriodLedgerStore {
    return CachedManualDutyPeriodLedgerStore(
        inner = inner,
        cache = newTtlCache<ManualDutyPeriodLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual duty period ledgers. Empty fields match all. */
data class ManualDutyPeriodLedgerQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDutyPeriodLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDutyPeriodLedger>): MutableList<ManualDutyPeriodLedger> {
        val matched = filter(items) { candidate: ManualDutyPeriodLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDutyPeriodLedgers(store: ManualDutyPeriodLedgerStore, query: ManualDutyPeriodLedgerQuery): MutableList<ManualDutyPeriodLedger> {
    return query.apply(store.list())
}
