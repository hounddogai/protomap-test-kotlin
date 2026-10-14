package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor agreements by code. */
interface EmissionFactorAgreementStore {
    fun find(code: String): EmissionFactorAgreement?
    fun save(item: EmissionFactorAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorAgreementStore(
    var items: MutableMap<String, EmissionFactorAgreement>,
    var order: MutableList<String>,
) : EmissionFactorAgreementStore {
    override fun find(code: String): EmissionFactorAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorAgreement) {
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

    override fun list(): MutableList<EmissionFactorAgreement> {
        val out = mutableListOf<EmissionFactorAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorAgreementStore(): EmissionFactorAgreementStore {
    return MemoryEmissionFactorAgreementStore(
        mutableMapOf<String, EmissionFactorAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorAgreementStore(
    var inner: EmissionFactorAgreementStore,
    var cache: TtlCache<EmissionFactorAgreement>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorAgreementStore {
    override fun find(code: String): EmissionFactorAgreement? {
        val hit = (cache.get(code) as EmissionFactorAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorAgreement> = inner.list()
}

fun newCachedEmissionFactorAgreementStore(inner: EmissionFactorAgreementStore, ttl: Int, clock: Clock): EmissionFactorAgreementStore {
    return CachedEmissionFactorAgreementStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor agreements. Empty fields match all. */
data class EmissionFactorAgreementQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorAgreement>): MutableList<EmissionFactorAgreement> {
        val matched = filter(items) { candidate: EmissionFactorAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorAgreements(store: EmissionFactorAgreementStore, query: EmissionFactorAgreementQuery): MutableList<EmissionFactorAgreement> {
    return query.apply(store.list())
}
