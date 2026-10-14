package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period agreements by code. */
interface DutyPeriodAgreementStore {
    fun find(code: String): DutyPeriodAgreement?
    fun save(item: DutyPeriodAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodAgreementStore(
    var items: MutableMap<String, DutyPeriodAgreement>,
    var order: MutableList<String>,
) : DutyPeriodAgreementStore {
    override fun find(code: String): DutyPeriodAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodAgreement) {
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

    override fun list(): MutableList<DutyPeriodAgreement> {
        val out = mutableListOf<DutyPeriodAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodAgreementStore(): DutyPeriodAgreementStore {
    return MemoryDutyPeriodAgreementStore(mutableMapOf<String, DutyPeriodAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodAgreementStore(
    var inner: DutyPeriodAgreementStore,
    var cache: TtlCache<DutyPeriodAgreement>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodAgreementStore {
    override fun find(code: String): DutyPeriodAgreement? {
        val hit = (cache.get(code) as DutyPeriodAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodAgreement> = inner.list()
}

fun newCachedDutyPeriodAgreementStore(inner: DutyPeriodAgreementStore, ttl: Int, clock: Clock): DutyPeriodAgreementStore {
    return CachedDutyPeriodAgreementStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period agreements. Empty fields match all. */
data class DutyPeriodAgreementQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodAgreement): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodAgreement>): MutableList<DutyPeriodAgreement> {
        val matched = filter(items) { candidate: DutyPeriodAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodAgreements(store: DutyPeriodAgreementStore, query: DutyPeriodAgreementQuery): MutableList<DutyPeriodAgreement> {
    return query.apply(store.list())
}
