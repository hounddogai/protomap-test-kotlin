package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive trailer agreements by code. */
interface AdaptiveTrailerAgreementStore {
    fun find(code: String): AdaptiveTrailerAgreement?
    fun save(item: AdaptiveTrailerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerAgreementStore(
    var items: MutableMap<String, AdaptiveTrailerAgreement>,
    var order: MutableList<String>,
) : AdaptiveTrailerAgreementStore {
    override fun find(code: String): AdaptiveTrailerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerAgreement) {
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

    override fun list(): MutableList<AdaptiveTrailerAgreement> {
        val out = mutableListOf<AdaptiveTrailerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerAgreementStore(): AdaptiveTrailerAgreementStore {
    return MemoryAdaptiveTrailerAgreementStore(
        mutableMapOf<String, AdaptiveTrailerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerAgreementStore(
    var inner: AdaptiveTrailerAgreementStore,
    var cache: TtlCache<AdaptiveTrailerAgreement>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerAgreementStore {
    override fun find(code: String): AdaptiveTrailerAgreement? {
        val hit = (cache.get(code) as AdaptiveTrailerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerAgreement> = inner.list()
}

fun newCachedAdaptiveTrailerAgreementStore(inner: AdaptiveTrailerAgreementStore, ttl: Int, clock: Clock): AdaptiveTrailerAgreementStore {
    return CachedAdaptiveTrailerAgreementStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer agreements. Empty fields match all. */
data class AdaptiveTrailerAgreementQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerAgreement): Boolean {
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

    fun apply(items: MutableList<AdaptiveTrailerAgreement>): MutableList<AdaptiveTrailerAgreement> {
        val matched = filter(items) { candidate: AdaptiveTrailerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerAgreements(store: AdaptiveTrailerAgreementStore, query: AdaptiveTrailerAgreementQuery): MutableList<AdaptiveTrailerAgreement> {
    return query.apply(store.list())
}
