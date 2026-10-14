package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff agreements by code. */
interface TariffAgreementStore {
    fun find(code: String): TariffAgreement?
    fun save(item: TariffAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffAgreementStore(
    var items: MutableMap<String, TariffAgreement>,
    var order: MutableList<String>,
) : TariffAgreementStore {
    override fun find(code: String): TariffAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffAgreement) {
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

    override fun list(): MutableList<TariffAgreement> {
        val out = mutableListOf<TariffAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffAgreementStore(): TariffAgreementStore {
    return MemoryTariffAgreementStore(mutableMapOf<String, TariffAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffAgreementStore(
    var inner: TariffAgreementStore,
    var cache: TtlCache<TariffAgreement>,
    var hits: Int,
    var misses: Int,
) : TariffAgreementStore {
    override fun find(code: String): TariffAgreement? {
        val hit = (cache.get(code) as TariffAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffAgreement> = inner.list()
}

fun newCachedTariffAgreementStore(inner: TariffAgreementStore, ttl: Int, clock: Clock): TariffAgreementStore {
    return CachedTariffAgreementStore(
        inner = inner,
        cache = newTtlCache<TariffAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff agreements. Empty fields match all. */
data class TariffAgreementQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffAgreement>): MutableList<TariffAgreement> {
        val matched = filter(items) { candidate: TariffAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffAgreements(store: TariffAgreementStore, query: TariffAgreementQuery): MutableList<TariffAgreement> {
    return query.apply(store.list())
}
