package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge agreements by code. */
interface TieredEvChargeAgreementStore {
    fun find(code: String): TieredEvChargeAgreement?
    fun save(item: TieredEvChargeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargeAgreementStore(
    var items: MutableMap<String, TieredEvChargeAgreement>,
    var order: MutableList<String>,
) : TieredEvChargeAgreementStore {
    override fun find(code: String): TieredEvChargeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargeAgreement) {
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

    override fun list(): MutableList<TieredEvChargeAgreement> {
        val out = mutableListOf<TieredEvChargeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargeAgreementStore(): TieredEvChargeAgreementStore {
    return MemoryTieredEvChargeAgreementStore(
        mutableMapOf<String, TieredEvChargeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargeAgreementStore(
    var inner: TieredEvChargeAgreementStore,
    var cache: TtlCache<TieredEvChargeAgreement>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargeAgreementStore {
    override fun find(code: String): TieredEvChargeAgreement? {
        val hit = (cache.get(code) as TieredEvChargeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargeAgreement> = inner.list()
}

fun newCachedTieredEvChargeAgreementStore(inner: TieredEvChargeAgreementStore, ttl: Int, clock: Clock): TieredEvChargeAgreementStore {
    return CachedTieredEvChargeAgreementStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge agreements. Empty fields match all. */
data class TieredEvChargeAgreementQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargeAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEvChargeAgreement>): MutableList<TieredEvChargeAgreement> {
        val matched = filter(items) { candidate: TieredEvChargeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargeAgreements(store: TieredEvChargeAgreementStore, query: TieredEvChargeAgreementQuery): MutableList<TieredEvChargeAgreement> {
    return query.apply(store.list())
}
