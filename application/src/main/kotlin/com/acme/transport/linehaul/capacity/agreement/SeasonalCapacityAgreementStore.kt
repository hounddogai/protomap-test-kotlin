package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal capacity agreements by code. */
interface SeasonalCapacityAgreementStore {
    fun find(code: String): SeasonalCapacityAgreement?
    fun save(item: SeasonalCapacityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCapacityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCapacityAgreementStore(
    var items: MutableMap<String, SeasonalCapacityAgreement>,
    var order: MutableList<String>,
) : SeasonalCapacityAgreementStore {
    override fun find(code: String): SeasonalCapacityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCapacityAgreement) {
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

    override fun list(): MutableList<SeasonalCapacityAgreement> {
        val out = mutableListOf<SeasonalCapacityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCapacityAgreementStore(): SeasonalCapacityAgreementStore {
    return MemorySeasonalCapacityAgreementStore(
        mutableMapOf<String, SeasonalCapacityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCapacityAgreementStore(
    var inner: SeasonalCapacityAgreementStore,
    var cache: TtlCache<SeasonalCapacityAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalCapacityAgreementStore {
    override fun find(code: String): SeasonalCapacityAgreement? {
        val hit = (cache.get(code) as SeasonalCapacityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCapacityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCapacityAgreement> = inner.list()
}

fun newCachedSeasonalCapacityAgreementStore(inner: SeasonalCapacityAgreementStore, ttl: Int, clock: Clock): SeasonalCapacityAgreementStore {
    return CachedSeasonalCapacityAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalCapacityAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal capacity agreements. Empty fields match all. */
data class SeasonalCapacityAgreementQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCapacityAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCapacityAgreement>): MutableList<SeasonalCapacityAgreement> {
        val matched = filter(items) { candidate: SeasonalCapacityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCapacityAgreements(store: SeasonalCapacityAgreementStore, query: SeasonalCapacityAgreementQuery): MutableList<SeasonalCapacityAgreement> {
    return query.apply(store.list())
}
