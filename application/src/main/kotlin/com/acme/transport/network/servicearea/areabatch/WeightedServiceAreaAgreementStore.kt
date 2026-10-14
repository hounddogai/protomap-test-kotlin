package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted service area agreements by code. */
interface WeightedServiceAreaAgreementStore {
    fun find(code: String): WeightedServiceAreaAgreement?
    fun save(item: WeightedServiceAreaAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServiceAreaAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServiceAreaAgreementStore(
    var items: MutableMap<String, WeightedServiceAreaAgreement>,
    var order: MutableList<String>,
) : WeightedServiceAreaAgreementStore {
    override fun find(code: String): WeightedServiceAreaAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServiceAreaAgreement) {
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

    override fun list(): MutableList<WeightedServiceAreaAgreement> {
        val out = mutableListOf<WeightedServiceAreaAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServiceAreaAgreementStore(): WeightedServiceAreaAgreementStore {
    return MemoryWeightedServiceAreaAgreementStore(
        mutableMapOf<String, WeightedServiceAreaAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServiceAreaAgreementStore(
    var inner: WeightedServiceAreaAgreementStore,
    var cache: TtlCache<WeightedServiceAreaAgreement>,
    var hits: Int,
    var misses: Int,
) : WeightedServiceAreaAgreementStore {
    override fun find(code: String): WeightedServiceAreaAgreement? {
        val hit = (cache.get(code) as WeightedServiceAreaAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServiceAreaAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServiceAreaAgreement> = inner.list()
}

fun newCachedWeightedServiceAreaAgreementStore(inner: WeightedServiceAreaAgreementStore, ttl: Int, clock: Clock): WeightedServiceAreaAgreementStore {
    return CachedWeightedServiceAreaAgreementStore(
        inner = inner,
        cache = newTtlCache<WeightedServiceAreaAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service area agreements. Empty fields match all. */
data class WeightedServiceAreaAgreementQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServiceAreaAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServiceAreaAgreement>): MutableList<WeightedServiceAreaAgreement> {
        val matched = filter(items) { candidate: WeightedServiceAreaAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServiceAreaAgreements(store: WeightedServiceAreaAgreementStore, query: WeightedServiceAreaAgreementQuery): MutableList<WeightedServiceAreaAgreement> {
    return query.apply(store.list())
}
