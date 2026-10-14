package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker agreements by code. */
interface AdaptiveLockerAgreementStore {
    fun find(code: String): AdaptiveLockerAgreement?
    fun save(item: AdaptiveLockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerAgreementStore(
    var items: MutableMap<String, AdaptiveLockerAgreement>,
    var order: MutableList<String>,
) : AdaptiveLockerAgreementStore {
    override fun find(code: String): AdaptiveLockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerAgreement) {
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

    override fun list(): MutableList<AdaptiveLockerAgreement> {
        val out = mutableListOf<AdaptiveLockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerAgreementStore(): AdaptiveLockerAgreementStore {
    return MemoryAdaptiveLockerAgreementStore(
        mutableMapOf<String, AdaptiveLockerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerAgreementStore(
    var inner: AdaptiveLockerAgreementStore,
    var cache: TtlCache<AdaptiveLockerAgreement>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerAgreementStore {
    override fun find(code: String): AdaptiveLockerAgreement? {
        val hit = (cache.get(code) as AdaptiveLockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerAgreement> = inner.list()
}

fun newCachedAdaptiveLockerAgreementStore(inner: AdaptiveLockerAgreementStore, ttl: Int, clock: Clock): AdaptiveLockerAgreementStore {
    return CachedAdaptiveLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker agreements. Empty fields match all. */
data class AdaptiveLockerAgreementQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveLockerAgreement>): MutableList<AdaptiveLockerAgreement> {
        val matched = filter(items) { candidate: AdaptiveLockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerAgreements(store: AdaptiveLockerAgreementStore, query: AdaptiveLockerAgreementQuery): MutableList<AdaptiveLockerAgreement> {
    return query.apply(store.list())
}
