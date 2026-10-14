package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary ev charge agreements by code. */
interface PrimaryEvChargeAgreementStore {
    fun find(code: String): PrimaryEvChargeAgreement?
    fun save(item: PrimaryEvChargeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryEvChargeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryEvChargeAgreementStore(
    var items: MutableMap<String, PrimaryEvChargeAgreement>,
    var order: MutableList<String>,
) : PrimaryEvChargeAgreementStore {
    override fun find(code: String): PrimaryEvChargeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryEvChargeAgreement) {
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

    override fun list(): MutableList<PrimaryEvChargeAgreement> {
        val out = mutableListOf<PrimaryEvChargeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryEvChargeAgreementStore(): PrimaryEvChargeAgreementStore {
    return MemoryPrimaryEvChargeAgreementStore(
        mutableMapOf<String, PrimaryEvChargeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryEvChargeAgreementStore(
    var inner: PrimaryEvChargeAgreementStore,
    var cache: TtlCache<PrimaryEvChargeAgreement>,
    var hits: Int,
    var misses: Int,
) : PrimaryEvChargeAgreementStore {
    override fun find(code: String): PrimaryEvChargeAgreement? {
        val hit = (cache.get(code) as PrimaryEvChargeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryEvChargeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryEvChargeAgreement> = inner.list()
}

fun newCachedPrimaryEvChargeAgreementStore(inner: PrimaryEvChargeAgreementStore, ttl: Int, clock: Clock): PrimaryEvChargeAgreementStore {
    return CachedPrimaryEvChargeAgreementStore(
        inner = inner,
        cache = newTtlCache<PrimaryEvChargeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary ev charge agreements. Empty fields match all. */
data class PrimaryEvChargeAgreementQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryEvChargeAgreement): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PrimaryEvChargeAgreement>): MutableList<PrimaryEvChargeAgreement> {
        val matched = filter(items) { candidate: PrimaryEvChargeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryEvChargeAgreements(store: PrimaryEvChargeAgreementStore, query: PrimaryEvChargeAgreementQuery): MutableList<PrimaryEvChargeAgreement> {
    return query.apply(store.list())
}
