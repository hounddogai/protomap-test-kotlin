package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan agreements by code. */
interface WeightedLanePlanAgreementStore {
    fun find(code: String): WeightedLanePlanAgreement?
    fun save(item: WeightedLanePlanAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanAgreementStore(
    var items: MutableMap<String, WeightedLanePlanAgreement>,
    var order: MutableList<String>,
) : WeightedLanePlanAgreementStore {
    override fun find(code: String): WeightedLanePlanAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanAgreement) {
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

    override fun list(): MutableList<WeightedLanePlanAgreement> {
        val out = mutableListOf<WeightedLanePlanAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanAgreementStore(): WeightedLanePlanAgreementStore {
    return MemoryWeightedLanePlanAgreementStore(
        mutableMapOf<String, WeightedLanePlanAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanAgreementStore(
    var inner: WeightedLanePlanAgreementStore,
    var cache: TtlCache<WeightedLanePlanAgreement>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanAgreementStore {
    override fun find(code: String): WeightedLanePlanAgreement? {
        val hit = (cache.get(code) as WeightedLanePlanAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanAgreement> = inner.list()
}

fun newCachedWeightedLanePlanAgreementStore(inner: WeightedLanePlanAgreementStore, ttl: Int, clock: Clock): WeightedLanePlanAgreementStore {
    return CachedWeightedLanePlanAgreementStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan agreements. Empty fields match all. */
data class WeightedLanePlanAgreementQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLanePlanAgreement>): MutableList<WeightedLanePlanAgreement> {
        val matched = filter(items) { candidate: WeightedLanePlanAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanAgreements(store: WeightedLanePlanAgreementStore, query: WeightedLanePlanAgreementQuery): MutableList<WeightedLanePlanAgreement> {
    return query.apply(store.list())
}
