package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor agreements by code. */
interface FallbackEmissionFactorAgreementStore {
    fun find(code: String): FallbackEmissionFactorAgreement?
    fun save(item: FallbackEmissionFactorAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorAgreementStore(
    var items: MutableMap<String, FallbackEmissionFactorAgreement>,
    var order: MutableList<String>,
) : FallbackEmissionFactorAgreementStore {
    override fun find(code: String): FallbackEmissionFactorAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorAgreement) {
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

    override fun list(): MutableList<FallbackEmissionFactorAgreement> {
        val out = mutableListOf<FallbackEmissionFactorAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorAgreementStore(): FallbackEmissionFactorAgreementStore {
    return MemoryFallbackEmissionFactorAgreementStore(
        mutableMapOf<String, FallbackEmissionFactorAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorAgreementStore(
    var inner: FallbackEmissionFactorAgreementStore,
    var cache: TtlCache<FallbackEmissionFactorAgreement>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorAgreementStore {
    override fun find(code: String): FallbackEmissionFactorAgreement? {
        val hit = (cache.get(code) as FallbackEmissionFactorAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorAgreement> = inner.list()
}

fun newCachedFallbackEmissionFactorAgreementStore(inner: FallbackEmissionFactorAgreementStore, ttl: Int, clock: Clock): FallbackEmissionFactorAgreementStore {
    return CachedFallbackEmissionFactorAgreementStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor agreements. Empty fields match all. */
data class FallbackEmissionFactorAgreementQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorAgreement>): MutableList<FallbackEmissionFactorAgreement> {
        val matched = filter(items) { candidate: FallbackEmissionFactorAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorAgreements(store: FallbackEmissionFactorAgreementStore, query: FallbackEmissionFactorAgreementQuery): MutableList<FallbackEmissionFactorAgreement> {
    return query.apply(store.list())
}
