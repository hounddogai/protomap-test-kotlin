package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive zone agreements by code. */
interface AdaptiveZoneAgreementStore {
    fun find(code: String): AdaptiveZoneAgreement?
    fun save(item: AdaptiveZoneAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveZoneAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveZoneAgreementStore(
    var items: MutableMap<String, AdaptiveZoneAgreement>,
    var order: MutableList<String>,
) : AdaptiveZoneAgreementStore {
    override fun find(code: String): AdaptiveZoneAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveZoneAgreement) {
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

    override fun list(): MutableList<AdaptiveZoneAgreement> {
        val out = mutableListOf<AdaptiveZoneAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveZoneAgreementStore(): AdaptiveZoneAgreementStore {
    return MemoryAdaptiveZoneAgreementStore(
        mutableMapOf<String, AdaptiveZoneAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveZoneAgreementStore(
    var inner: AdaptiveZoneAgreementStore,
    var cache: TtlCache<AdaptiveZoneAgreement>,
    var hits: Int,
    var misses: Int,
) : AdaptiveZoneAgreementStore {
    override fun find(code: String): AdaptiveZoneAgreement? {
        val hit = (cache.get(code) as AdaptiveZoneAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveZoneAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveZoneAgreement> = inner.list()
}

fun newCachedAdaptiveZoneAgreementStore(inner: AdaptiveZoneAgreementStore, ttl: Int, clock: Clock): AdaptiveZoneAgreementStore {
    return CachedAdaptiveZoneAgreementStore(
        inner = inner,
        cache = newTtlCache<AdaptiveZoneAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive zone agreements. Empty fields match all. */
data class AdaptiveZoneAgreementQuery(
    var source: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveZoneAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveZoneAgreement>): MutableList<AdaptiveZoneAgreement> {
        val matched = filter(items) { candidate: AdaptiveZoneAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveZoneAgreements(store: AdaptiveZoneAgreementStore, query: AdaptiveZoneAgreementQuery): MutableList<AdaptiveZoneAgreement> {
    return query.apply(store.list())
}
