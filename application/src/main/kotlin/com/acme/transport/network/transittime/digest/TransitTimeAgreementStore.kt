package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time agreements by code. */
interface TransitTimeAgreementStore {
    fun find(code: String): TransitTimeAgreement?
    fun save(item: TransitTimeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeAgreementStore(
    var items: MutableMap<String, TransitTimeAgreement>,
    var order: MutableList<String>,
) : TransitTimeAgreementStore {
    override fun find(code: String): TransitTimeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeAgreement) {
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

    override fun list(): MutableList<TransitTimeAgreement> {
        val out = mutableListOf<TransitTimeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeAgreementStore(): TransitTimeAgreementStore {
    return MemoryTransitTimeAgreementStore(
        mutableMapOf<String, TransitTimeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeAgreementStore(
    var inner: TransitTimeAgreementStore,
    var cache: TtlCache<TransitTimeAgreement>,
    var hits: Int,
    var misses: Int,
) : TransitTimeAgreementStore {
    override fun find(code: String): TransitTimeAgreement? {
        val hit = (cache.get(code) as TransitTimeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeAgreement> = inner.list()
}

fun newCachedTransitTimeAgreementStore(inner: TransitTimeAgreementStore, ttl: Int, clock: Clock): TransitTimeAgreementStore {
    return CachedTransitTimeAgreementStore(
        inner = inner,
        cache = newTtlCache<TransitTimeAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time agreements. Empty fields match all. */
data class TransitTimeAgreementQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeAgreement>): MutableList<TransitTimeAgreement> {
        val matched = filter(items) { candidate: TransitTimeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeAgreements(store: TransitTimeAgreementStore, query: TransitTimeAgreementQuery): MutableList<TransitTimeAgreement> {
    return query.apply(store.list())
}
