package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tariff agreements by code. */
interface DefaultTariffAgreementStore {
    fun find(code: String): DefaultTariffAgreement?
    fun save(item: DefaultTariffAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTariffAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTariffAgreementStore(
    var items: MutableMap<String, DefaultTariffAgreement>,
    var order: MutableList<String>,
) : DefaultTariffAgreementStore {
    override fun find(code: String): DefaultTariffAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTariffAgreement) {
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

    override fun list(): MutableList<DefaultTariffAgreement> {
        val out = mutableListOf<DefaultTariffAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTariffAgreementStore(): DefaultTariffAgreementStore {
    return MemoryDefaultTariffAgreementStore(
        mutableMapOf<String, DefaultTariffAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTariffAgreementStore(
    var inner: DefaultTariffAgreementStore,
    var cache: TtlCache<DefaultTariffAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultTariffAgreementStore {
    override fun find(code: String): DefaultTariffAgreement? {
        val hit = (cache.get(code) as DefaultTariffAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTariffAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTariffAgreement> = inner.list()
}

fun newCachedDefaultTariffAgreementStore(inner: DefaultTariffAgreementStore, ttl: Int, clock: Clock): DefaultTariffAgreementStore {
    return CachedDefaultTariffAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultTariffAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tariff agreements. Empty fields match all. */
data class DefaultTariffAgreementQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTariffAgreement): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTariffAgreement>): MutableList<DefaultTariffAgreement> {
        val matched = filter(items) { candidate: DefaultTariffAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTariffAgreements(store: DefaultTariffAgreementStore, query: DefaultTariffAgreementQuery): MutableList<DefaultTariffAgreement> {
    return query.apply(store.list())
}
