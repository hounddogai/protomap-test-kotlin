package com.acme.transport.customs.broker.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker agreements by code. */
interface BrokerAgreementStore {
    fun find(code: String): BrokerAgreement?
    fun save(item: BrokerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerAgreementStore(
    var items: MutableMap<String, BrokerAgreement>,
    var order: MutableList<String>,
) : BrokerAgreementStore {
    override fun find(code: String): BrokerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerAgreement) {
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

    override fun list(): MutableList<BrokerAgreement> {
        val out = mutableListOf<BrokerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerAgreementStore(): BrokerAgreementStore {
    return MemoryBrokerAgreementStore(mutableMapOf<String, BrokerAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerAgreementStore(
    var inner: BrokerAgreementStore,
    var cache: TtlCache<BrokerAgreement>,
    var hits: Int,
    var misses: Int,
) : BrokerAgreementStore {
    override fun find(code: String): BrokerAgreement? {
        val hit = (cache.get(code) as BrokerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerAgreement> = inner.list()
}

fun newCachedBrokerAgreementStore(inner: BrokerAgreementStore, ttl: Int, clock: Clock): BrokerAgreementStore {
    return CachedBrokerAgreementStore(
        inner = inner,
        cache = newTtlCache<BrokerAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker agreements. Empty fields match all. */
data class BrokerAgreementQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerAgreement>): MutableList<BrokerAgreement> {
        val matched = filter(items) { candidate: BrokerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerAgreements(store: BrokerAgreementStore, query: BrokerAgreementQuery): MutableList<BrokerAgreement> {
    return query.apply(store.list())
}
