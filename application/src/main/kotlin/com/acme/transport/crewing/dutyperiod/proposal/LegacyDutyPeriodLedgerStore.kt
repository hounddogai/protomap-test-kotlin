package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy duty period ledgers by code. */
interface LegacyDutyPeriodLedgerStore {
    fun find(code: String): LegacyDutyPeriodLedger?
    fun save(item: LegacyDutyPeriodLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDutyPeriodLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDutyPeriodLedgerStore(
    var items: MutableMap<String, LegacyDutyPeriodLedger>,
    var order: MutableList<String>,
) : LegacyDutyPeriodLedgerStore {
    override fun find(code: String): LegacyDutyPeriodLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDutyPeriodLedger) {
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

    override fun list(): MutableList<LegacyDutyPeriodLedger> {
        val out = mutableListOf<LegacyDutyPeriodLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDutyPeriodLedgerStore(): LegacyDutyPeriodLedgerStore {
    return MemoryLegacyDutyPeriodLedgerStore(
        mutableMapOf<String, LegacyDutyPeriodLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDutyPeriodLedgerStore(
    var inner: LegacyDutyPeriodLedgerStore,
    var cache: TtlCache<LegacyDutyPeriodLedger>,
    var hits: Int,
    var misses: Int,
) : LegacyDutyPeriodLedgerStore {
    override fun find(code: String): LegacyDutyPeriodLedger? {
        val hit = (cache.get(code) as LegacyDutyPeriodLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDutyPeriodLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDutyPeriodLedger> = inner.list()
}

fun newCachedLegacyDutyPeriodLedgerStore(inner: LegacyDutyPeriodLedgerStore, ttl: Int, clock: Clock): LegacyDutyPeriodLedgerStore {
    return CachedLegacyDutyPeriodLedgerStore(
        inner = inner,
        cache = newTtlCache<LegacyDutyPeriodLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy duty period ledgers. Empty fields match all. */
data class LegacyDutyPeriodLedgerQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDutyPeriodLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDutyPeriodLedger>): MutableList<LegacyDutyPeriodLedger> {
        val matched = filter(items) { candidate: LegacyDutyPeriodLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDutyPeriodLedgers(store: LegacyDutyPeriodLedgerStore, query: LegacyDutyPeriodLedgerQuery): MutableList<LegacyDutyPeriodLedger> {
    return query.apply(store.list())
}
