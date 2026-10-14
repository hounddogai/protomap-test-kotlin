package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard agreements by code. */
interface ScorecardAgreementStore {
    fun find(code: String): ScorecardAgreement?
    fun save(item: ScorecardAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardAgreementStore(
    var items: MutableMap<String, ScorecardAgreement>,
    var order: MutableList<String>,
) : ScorecardAgreementStore {
    override fun find(code: String): ScorecardAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardAgreement) {
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

    override fun list(): MutableList<ScorecardAgreement> {
        val out = mutableListOf<ScorecardAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardAgreementStore(): ScorecardAgreementStore {
    return MemoryScorecardAgreementStore(mutableMapOf<String, ScorecardAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardAgreementStore(
    var inner: ScorecardAgreementStore,
    var cache: TtlCache<ScorecardAgreement>,
    var hits: Int,
    var misses: Int,
) : ScorecardAgreementStore {
    override fun find(code: String): ScorecardAgreement? {
        val hit = (cache.get(code) as ScorecardAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardAgreement> = inner.list()
}

fun newCachedScorecardAgreementStore(inner: ScorecardAgreementStore, ttl: Int, clock: Clock): ScorecardAgreementStore {
    return CachedScorecardAgreementStore(
        inner = inner,
        cache = newTtlCache<ScorecardAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard agreements. Empty fields match all. */
data class ScorecardAgreementQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardAgreement>): MutableList<ScorecardAgreement> {
        val matched = filter(items) { candidate: ScorecardAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardAgreements(store: ScorecardAgreementStore, query: ScorecardAgreementQuery): MutableList<ScorecardAgreement> {
    return query.apply(store.list())
}
