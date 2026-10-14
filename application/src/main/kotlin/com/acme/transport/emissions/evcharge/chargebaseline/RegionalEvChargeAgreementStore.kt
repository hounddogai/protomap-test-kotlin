package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional ev charge agreements by code. */
interface RegionalEvChargeAgreementStore {
    fun find(code: String): RegionalEvChargeAgreement?
    fun save(item: RegionalEvChargeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalEvChargeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalEvChargeAgreementStore(
    var items: MutableMap<String, RegionalEvChargeAgreement>,
    var order: MutableList<String>,
) : RegionalEvChargeAgreementStore {
    override fun find(code: String): RegionalEvChargeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalEvChargeAgreement) {
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

    override fun list(): MutableList<RegionalEvChargeAgreement> {
        val out = mutableListOf<RegionalEvChargeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalEvChargeAgreementStore(): RegionalEvChargeAgreementStore {
    return MemoryRegionalEvChargeAgreementStore(
        mutableMapOf<String, RegionalEvChargeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalEvChargeAgreementStore(
    var inner: RegionalEvChargeAgreementStore,
    var cache: TtlCache<RegionalEvChargeAgreement>,
    var hits: Int,
    var misses: Int,
) : RegionalEvChargeAgreementStore {
    override fun find(code: String): RegionalEvChargeAgreement? {
        val hit = (cache.get(code) as RegionalEvChargeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalEvChargeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalEvChargeAgreement> = inner.list()
}

fun newCachedRegionalEvChargeAgreementStore(inner: RegionalEvChargeAgreementStore, ttl: Int, clock: Clock): RegionalEvChargeAgreementStore {
    return CachedRegionalEvChargeAgreementStore(
        inner = inner,
        cache = newTtlCache<RegionalEvChargeAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional ev charge agreements. Empty fields match all. */
data class RegionalEvChargeAgreementQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalEvChargeAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalEvChargeAgreement>): MutableList<RegionalEvChargeAgreement> {
        val matched = filter(items) { candidate: RegionalEvChargeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalEvChargeAgreements(store: RegionalEvChargeAgreementStore, query: RegionalEvChargeAgreementQuery): MutableList<RegionalEvChargeAgreement> {
    return query.apply(store.list())
}
