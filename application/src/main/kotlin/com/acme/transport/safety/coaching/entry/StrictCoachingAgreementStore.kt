package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict coaching agreements by code. */
interface StrictCoachingAgreementStore {
    fun find(code: String): StrictCoachingAgreement?
    fun save(item: StrictCoachingAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCoachingAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCoachingAgreementStore(
    var items: MutableMap<String, StrictCoachingAgreement>,
    var order: MutableList<String>,
) : StrictCoachingAgreementStore {
    override fun find(code: String): StrictCoachingAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCoachingAgreement) {
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

    override fun list(): MutableList<StrictCoachingAgreement> {
        val out = mutableListOf<StrictCoachingAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCoachingAgreementStore(): StrictCoachingAgreementStore {
    return MemoryStrictCoachingAgreementStore(
        mutableMapOf<String, StrictCoachingAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCoachingAgreementStore(
    var inner: StrictCoachingAgreementStore,
    var cache: TtlCache<StrictCoachingAgreement>,
    var hits: Int,
    var misses: Int,
) : StrictCoachingAgreementStore {
    override fun find(code: String): StrictCoachingAgreement? {
        val hit = (cache.get(code) as StrictCoachingAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCoachingAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCoachingAgreement> = inner.list()
}

fun newCachedStrictCoachingAgreementStore(inner: StrictCoachingAgreementStore, ttl: Int, clock: Clock): StrictCoachingAgreementStore {
    return CachedStrictCoachingAgreementStore(
        inner = inner,
        cache = newTtlCache<StrictCoachingAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict coaching agreements. Empty fields match all. */
data class StrictCoachingAgreementQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCoachingAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictCoachingAgreement>): MutableList<StrictCoachingAgreement> {
        val matched = filter(items) { candidate: StrictCoachingAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCoachingAgreements(store: StrictCoachingAgreementStore, query: StrictCoachingAgreementQuery): MutableList<StrictCoachingAgreement> {
    return query.apply(store.list())
}
