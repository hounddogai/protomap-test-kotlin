package com.acme.transport.network.node.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal node agreements by code. */
interface SeasonalNodeAgreementStore {
    fun find(code: String): SeasonalNodeAgreement?
    fun save(item: SeasonalNodeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalNodeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalNodeAgreementStore(
    var items: MutableMap<String, SeasonalNodeAgreement>,
    var order: MutableList<String>,
) : SeasonalNodeAgreementStore {
    override fun find(code: String): SeasonalNodeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalNodeAgreement) {
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

    override fun list(): MutableList<SeasonalNodeAgreement> {
        val out = mutableListOf<SeasonalNodeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalNodeAgreementStore(): SeasonalNodeAgreementStore {
    return MemorySeasonalNodeAgreementStore(
        mutableMapOf<String, SeasonalNodeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalNodeAgreementStore(
    var inner: SeasonalNodeAgreementStore,
    var cache: TtlCache<SeasonalNodeAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalNodeAgreementStore {
    override fun find(code: String): SeasonalNodeAgreement? {
        val hit = (cache.get(code) as SeasonalNodeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalNodeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalNodeAgreement> = inner.list()
}

fun newCachedSeasonalNodeAgreementStore(inner: SeasonalNodeAgreementStore, ttl: Int, clock: Clock): SeasonalNodeAgreementStore {
    return CachedSeasonalNodeAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalNodeAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal node agreements. Empty fields match all. */
data class SeasonalNodeAgreementQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalNodeAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalNodeAgreement>): MutableList<SeasonalNodeAgreement> {
        val matched = filter(items) { candidate: SeasonalNodeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalNodeAgreements(store: SeasonalNodeAgreementStore, query: SeasonalNodeAgreementQuery): MutableList<SeasonalNodeAgreement> {
    return query.apply(store.list())
}
