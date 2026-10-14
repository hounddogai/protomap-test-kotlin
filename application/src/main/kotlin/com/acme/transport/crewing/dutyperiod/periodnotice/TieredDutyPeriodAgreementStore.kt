package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered duty period agreements by code. */
interface TieredDutyPeriodAgreementStore {
    fun find(code: String): TieredDutyPeriodAgreement?
    fun save(item: TieredDutyPeriodAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDutyPeriodAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDutyPeriodAgreementStore(
    var items: MutableMap<String, TieredDutyPeriodAgreement>,
    var order: MutableList<String>,
) : TieredDutyPeriodAgreementStore {
    override fun find(code: String): TieredDutyPeriodAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDutyPeriodAgreement) {
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

    override fun list(): MutableList<TieredDutyPeriodAgreement> {
        val out = mutableListOf<TieredDutyPeriodAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDutyPeriodAgreementStore(): TieredDutyPeriodAgreementStore {
    return MemoryTieredDutyPeriodAgreementStore(
        mutableMapOf<String, TieredDutyPeriodAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDutyPeriodAgreementStore(
    var inner: TieredDutyPeriodAgreementStore,
    var cache: TtlCache<TieredDutyPeriodAgreement>,
    var hits: Int,
    var misses: Int,
) : TieredDutyPeriodAgreementStore {
    override fun find(code: String): TieredDutyPeriodAgreement? {
        val hit = (cache.get(code) as TieredDutyPeriodAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDutyPeriodAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDutyPeriodAgreement> = inner.list()
}

fun newCachedTieredDutyPeriodAgreementStore(inner: TieredDutyPeriodAgreementStore, ttl: Int, clock: Clock): TieredDutyPeriodAgreementStore {
    return CachedTieredDutyPeriodAgreementStore(
        inner = inner,
        cache = newTtlCache<TieredDutyPeriodAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered duty period agreements. Empty fields match all. */
data class TieredDutyPeriodAgreementQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDutyPeriodAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDutyPeriodAgreement>): MutableList<TieredDutyPeriodAgreement> {
        val matched = filter(items) { candidate: TieredDutyPeriodAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDutyPeriodAgreements(store: TieredDutyPeriodAgreementStore, query: TieredDutyPeriodAgreementQuery): MutableList<TieredDutyPeriodAgreement> {
    return query.apply(store.list())
}
