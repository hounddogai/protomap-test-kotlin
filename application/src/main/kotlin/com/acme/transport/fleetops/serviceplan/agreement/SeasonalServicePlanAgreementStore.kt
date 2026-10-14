package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan agreements by code. */
interface SeasonalServicePlanAgreementStore {
    fun find(code: String): SeasonalServicePlanAgreement?
    fun save(item: SeasonalServicePlanAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanAgreementStore(
    var items: MutableMap<String, SeasonalServicePlanAgreement>,
    var order: MutableList<String>,
) : SeasonalServicePlanAgreementStore {
    override fun find(code: String): SeasonalServicePlanAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanAgreement) {
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

    override fun list(): MutableList<SeasonalServicePlanAgreement> {
        val out = mutableListOf<SeasonalServicePlanAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanAgreementStore(): SeasonalServicePlanAgreementStore {
    return MemorySeasonalServicePlanAgreementStore(
        mutableMapOf<String, SeasonalServicePlanAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanAgreementStore(
    var inner: SeasonalServicePlanAgreementStore,
    var cache: TtlCache<SeasonalServicePlanAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanAgreementStore {
    override fun find(code: String): SeasonalServicePlanAgreement? {
        val hit = (cache.get(code) as SeasonalServicePlanAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanAgreement> = inner.list()
}

fun newCachedSeasonalServicePlanAgreementStore(inner: SeasonalServicePlanAgreementStore, ttl: Int, clock: Clock): SeasonalServicePlanAgreementStore {
    return CachedSeasonalServicePlanAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan agreements. Empty fields match all. */
data class SeasonalServicePlanAgreementQuery(
    var segment: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServicePlanAgreement>): MutableList<SeasonalServicePlanAgreement> {
        val matched = filter(items) { candidate: SeasonalServicePlanAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanAgreements(store: SeasonalServicePlanAgreementStore, query: SeasonalServicePlanAgreementQuery): MutableList<SeasonalServicePlanAgreement> {
    return query.apply(store.list())
}
