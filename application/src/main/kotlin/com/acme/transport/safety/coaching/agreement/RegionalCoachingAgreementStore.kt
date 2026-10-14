package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional coaching agreements by code. */
interface RegionalCoachingAgreementStore {
    fun find(code: String): RegionalCoachingAgreement?
    fun save(item: RegionalCoachingAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCoachingAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCoachingAgreementStore(
    var items: MutableMap<String, RegionalCoachingAgreement>,
    var order: MutableList<String>,
) : RegionalCoachingAgreementStore {
    override fun find(code: String): RegionalCoachingAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCoachingAgreement) {
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

    override fun list(): MutableList<RegionalCoachingAgreement> {
        val out = mutableListOf<RegionalCoachingAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCoachingAgreementStore(): RegionalCoachingAgreementStore {
    return MemoryRegionalCoachingAgreementStore(
        mutableMapOf<String, RegionalCoachingAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCoachingAgreementStore(
    var inner: RegionalCoachingAgreementStore,
    var cache: TtlCache<RegionalCoachingAgreement>,
    var hits: Int,
    var misses: Int,
) : RegionalCoachingAgreementStore {
    override fun find(code: String): RegionalCoachingAgreement? {
        val hit = (cache.get(code) as RegionalCoachingAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCoachingAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCoachingAgreement> = inner.list()
}

fun newCachedRegionalCoachingAgreementStore(inner: RegionalCoachingAgreementStore, ttl: Int, clock: Clock): RegionalCoachingAgreementStore {
    return CachedRegionalCoachingAgreementStore(
        inner = inner,
        cache = newTtlCache<RegionalCoachingAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional coaching agreements. Empty fields match all. */
data class RegionalCoachingAgreementQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCoachingAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCoachingAgreement>): MutableList<RegionalCoachingAgreement> {
        val matched = filter(items) { candidate: RegionalCoachingAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCoachingAgreements(store: RegionalCoachingAgreementStore, query: RegionalCoachingAgreementQuery): MutableList<RegionalCoachingAgreement> {
    return query.apply(store.list())
}
