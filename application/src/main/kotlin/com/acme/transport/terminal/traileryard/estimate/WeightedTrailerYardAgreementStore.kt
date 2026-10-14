package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard agreements by code. */
interface WeightedTrailerYardAgreementStore {
    fun find(code: String): WeightedTrailerYardAgreement?
    fun save(item: WeightedTrailerYardAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardAgreementStore(
    var items: MutableMap<String, WeightedTrailerYardAgreement>,
    var order: MutableList<String>,
) : WeightedTrailerYardAgreementStore {
    override fun find(code: String): WeightedTrailerYardAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardAgreement) {
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

    override fun list(): MutableList<WeightedTrailerYardAgreement> {
        val out = mutableListOf<WeightedTrailerYardAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardAgreementStore(): WeightedTrailerYardAgreementStore {
    return MemoryWeightedTrailerYardAgreementStore(
        mutableMapOf<String, WeightedTrailerYardAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardAgreementStore(
    var inner: WeightedTrailerYardAgreementStore,
    var cache: TtlCache<WeightedTrailerYardAgreement>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardAgreementStore {
    override fun find(code: String): WeightedTrailerYardAgreement? {
        val hit = (cache.get(code) as WeightedTrailerYardAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardAgreement> = inner.list()
}

fun newCachedWeightedTrailerYardAgreementStore(inner: WeightedTrailerYardAgreementStore, ttl: Int, clock: Clock): WeightedTrailerYardAgreementStore {
    return CachedWeightedTrailerYardAgreementStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard agreements. Empty fields match all. */
data class WeightedTrailerYardAgreementQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardAgreement>): MutableList<WeightedTrailerYardAgreement> {
        val matched = filter(items) { candidate: WeightedTrailerYardAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardAgreements(store: WeightedTrailerYardAgreementStore, query: WeightedTrailerYardAgreementQuery): MutableList<WeightedTrailerYardAgreement> {
    return query.apply(store.list())
}
