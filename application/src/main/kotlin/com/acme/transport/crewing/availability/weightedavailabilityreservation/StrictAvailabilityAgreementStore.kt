package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict availability agreements by code. */
interface StrictAvailabilityAgreementStore {
    fun find(code: String): StrictAvailabilityAgreement?
    fun save(item: StrictAvailabilityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAvailabilityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAvailabilityAgreementStore(
    var items: MutableMap<String, StrictAvailabilityAgreement>,
    var order: MutableList<String>,
) : StrictAvailabilityAgreementStore {
    override fun find(code: String): StrictAvailabilityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAvailabilityAgreement) {
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

    override fun list(): MutableList<StrictAvailabilityAgreement> {
        val out = mutableListOf<StrictAvailabilityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAvailabilityAgreementStore(): StrictAvailabilityAgreementStore {
    return MemoryStrictAvailabilityAgreementStore(
        mutableMapOf<String, StrictAvailabilityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAvailabilityAgreementStore(
    var inner: StrictAvailabilityAgreementStore,
    var cache: TtlCache<StrictAvailabilityAgreement>,
    var hits: Int,
    var misses: Int,
) : StrictAvailabilityAgreementStore {
    override fun find(code: String): StrictAvailabilityAgreement? {
        val hit = (cache.get(code) as StrictAvailabilityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAvailabilityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAvailabilityAgreement> = inner.list()
}

fun newCachedStrictAvailabilityAgreementStore(inner: StrictAvailabilityAgreementStore, ttl: Int, clock: Clock): StrictAvailabilityAgreementStore {
    return CachedStrictAvailabilityAgreementStore(
        inner = inner,
        cache = newTtlCache<StrictAvailabilityAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict availability agreements. Empty fields match all. */
data class StrictAvailabilityAgreementQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAvailabilityAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<StrictAvailabilityAgreement>): MutableList<StrictAvailabilityAgreement> {
        val matched = filter(items) { candidate: StrictAvailabilityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAvailabilityAgreements(store: StrictAvailabilityAgreementStore, query: StrictAvailabilityAgreementQuery): MutableList<StrictAvailabilityAgreement> {
    return query.apply(store.list())
}
