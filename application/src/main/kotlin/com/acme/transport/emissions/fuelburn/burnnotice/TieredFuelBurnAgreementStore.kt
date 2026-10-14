package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel burn agreements by code. */
interface TieredFuelBurnAgreementStore {
    fun find(code: String): TieredFuelBurnAgreement?
    fun save(item: TieredFuelBurnAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelBurnAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelBurnAgreementStore(
    var items: MutableMap<String, TieredFuelBurnAgreement>,
    var order: MutableList<String>,
) : TieredFuelBurnAgreementStore {
    override fun find(code: String): TieredFuelBurnAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelBurnAgreement) {
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

    override fun list(): MutableList<TieredFuelBurnAgreement> {
        val out = mutableListOf<TieredFuelBurnAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelBurnAgreementStore(): TieredFuelBurnAgreementStore {
    return MemoryTieredFuelBurnAgreementStore(
        mutableMapOf<String, TieredFuelBurnAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelBurnAgreementStore(
    var inner: TieredFuelBurnAgreementStore,
    var cache: TtlCache<TieredFuelBurnAgreement>,
    var hits: Int,
    var misses: Int,
) : TieredFuelBurnAgreementStore {
    override fun find(code: String): TieredFuelBurnAgreement? {
        val hit = (cache.get(code) as TieredFuelBurnAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelBurnAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelBurnAgreement> = inner.list()
}

fun newCachedTieredFuelBurnAgreementStore(inner: TieredFuelBurnAgreementStore, ttl: Int, clock: Clock): TieredFuelBurnAgreementStore {
    return CachedTieredFuelBurnAgreementStore(
        inner = inner,
        cache = newTtlCache<TieredFuelBurnAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel burn agreements. Empty fields match all. */
data class TieredFuelBurnAgreementQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelBurnAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFuelBurnAgreement>): MutableList<TieredFuelBurnAgreement> {
        val matched = filter(items) { candidate: TieredFuelBurnAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelBurnAgreements(store: TieredFuelBurnAgreementStore, query: TieredFuelBurnAgreementQuery): MutableList<TieredFuelBurnAgreement> {
    return query.apply(store.list())
}
