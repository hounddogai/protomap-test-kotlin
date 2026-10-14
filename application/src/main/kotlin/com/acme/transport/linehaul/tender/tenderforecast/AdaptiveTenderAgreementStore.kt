package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive tender agreements by code. */
interface AdaptiveTenderAgreementStore {
    fun find(code: String): AdaptiveTenderAgreement?
    fun save(item: AdaptiveTenderAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTenderAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTenderAgreementStore(
    var items: MutableMap<String, AdaptiveTenderAgreement>,
    var order: MutableList<String>,
) : AdaptiveTenderAgreementStore {
    override fun find(code: String): AdaptiveTenderAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTenderAgreement) {
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

    override fun list(): MutableList<AdaptiveTenderAgreement> {
        val out = mutableListOf<AdaptiveTenderAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTenderAgreementStore(): AdaptiveTenderAgreementStore {
    return MemoryAdaptiveTenderAgreementStore(
        mutableMapOf<String, AdaptiveTenderAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTenderAgreementStore(
    var inner: AdaptiveTenderAgreementStore,
    var cache: TtlCache<AdaptiveTenderAgreement>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTenderAgreementStore {
    override fun find(code: String): AdaptiveTenderAgreement? {
        val hit = (cache.get(code) as AdaptiveTenderAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTenderAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTenderAgreement> = inner.list()
}

fun newCachedAdaptiveTenderAgreementStore(inner: AdaptiveTenderAgreementStore, ttl: Int, clock: Clock): AdaptiveTenderAgreementStore {
    return CachedAdaptiveTenderAgreementStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTenderAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tender agreements. Empty fields match all. */
data class AdaptiveTenderAgreementQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTenderAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTenderAgreement>): MutableList<AdaptiveTenderAgreement> {
        val matched = filter(items) { candidate: AdaptiveTenderAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTenderAgreements(store: AdaptiveTenderAgreementStore, query: AdaptiveTenderAgreementQuery): MutableList<AdaptiveTenderAgreement> {
    return query.apply(store.list())
}
