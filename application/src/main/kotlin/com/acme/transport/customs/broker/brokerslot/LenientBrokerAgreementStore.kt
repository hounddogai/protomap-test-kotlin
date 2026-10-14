package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient broker agreements by code. */
interface LenientBrokerAgreementStore {
    fun find(code: String): LenientBrokerAgreement?
    fun save(item: LenientBrokerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBrokerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBrokerAgreementStore(
    var items: MutableMap<String, LenientBrokerAgreement>,
    var order: MutableList<String>,
) : LenientBrokerAgreementStore {
    override fun find(code: String): LenientBrokerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBrokerAgreement) {
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

    override fun list(): MutableList<LenientBrokerAgreement> {
        val out = mutableListOf<LenientBrokerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBrokerAgreementStore(): LenientBrokerAgreementStore {
    return MemoryLenientBrokerAgreementStore(
        mutableMapOf<String, LenientBrokerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBrokerAgreementStore(
    var inner: LenientBrokerAgreementStore,
    var cache: TtlCache<LenientBrokerAgreement>,
    var hits: Int,
    var misses: Int,
) : LenientBrokerAgreementStore {
    override fun find(code: String): LenientBrokerAgreement? {
        val hit = (cache.get(code) as LenientBrokerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBrokerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBrokerAgreement> = inner.list()
}

fun newCachedLenientBrokerAgreementStore(inner: LenientBrokerAgreementStore, ttl: Int, clock: Clock): LenientBrokerAgreementStore {
    return CachedLenientBrokerAgreementStore(
        inner = inner,
        cache = newTtlCache<LenientBrokerAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient broker agreements. Empty fields match all. */
data class LenientBrokerAgreementQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBrokerAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientBrokerAgreement>): MutableList<LenientBrokerAgreement> {
        val matched = filter(items) { candidate: LenientBrokerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBrokerAgreements(store: LenientBrokerAgreementStore, query: LenientBrokerAgreementQuery): MutableList<LenientBrokerAgreement> {
    return query.apply(store.list())
}
