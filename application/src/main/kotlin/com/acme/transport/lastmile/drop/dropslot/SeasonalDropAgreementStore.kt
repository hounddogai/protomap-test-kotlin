package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal drop agreements by code. */
interface SeasonalDropAgreementStore {
    fun find(code: String): SeasonalDropAgreement?
    fun save(item: SeasonalDropAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDropAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDropAgreementStore(
    var items: MutableMap<String, SeasonalDropAgreement>,
    var order: MutableList<String>,
) : SeasonalDropAgreementStore {
    override fun find(code: String): SeasonalDropAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDropAgreement) {
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

    override fun list(): MutableList<SeasonalDropAgreement> {
        val out = mutableListOf<SeasonalDropAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDropAgreementStore(): SeasonalDropAgreementStore {
    return MemorySeasonalDropAgreementStore(
        mutableMapOf<String, SeasonalDropAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDropAgreementStore(
    var inner: SeasonalDropAgreementStore,
    var cache: TtlCache<SeasonalDropAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalDropAgreementStore {
    override fun find(code: String): SeasonalDropAgreement? {
        val hit = (cache.get(code) as SeasonalDropAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDropAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDropAgreement> = inner.list()
}

fun newCachedSeasonalDropAgreementStore(inner: SeasonalDropAgreementStore, ttl: Int, clock: Clock): SeasonalDropAgreementStore {
    return CachedSeasonalDropAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalDropAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal drop agreements. Empty fields match all. */
data class SeasonalDropAgreementQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDropAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDropAgreement>): MutableList<SeasonalDropAgreement> {
        val matched = filter(items) { candidate: SeasonalDropAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDropAgreements(store: SeasonalDropAgreementStore, query: SeasonalDropAgreementQuery): MutableList<SeasonalDropAgreement> {
    return query.apply(store.list())
}
