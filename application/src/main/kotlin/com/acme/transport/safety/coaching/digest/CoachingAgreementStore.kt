package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching agreements by code. */
interface CoachingAgreementStore {
    fun find(code: String): CoachingAgreement?
    fun save(item: CoachingAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingAgreementStore(
    var items: MutableMap<String, CoachingAgreement>,
    var order: MutableList<String>,
) : CoachingAgreementStore {
    override fun find(code: String): CoachingAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingAgreement) {
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

    override fun list(): MutableList<CoachingAgreement> {
        val out = mutableListOf<CoachingAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingAgreementStore(): CoachingAgreementStore {
    return MemoryCoachingAgreementStore(mutableMapOf<String, CoachingAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingAgreementStore(
    var inner: CoachingAgreementStore,
    var cache: TtlCache<CoachingAgreement>,
    var hits: Int,
    var misses: Int,
) : CoachingAgreementStore {
    override fun find(code: String): CoachingAgreement? {
        val hit = (cache.get(code) as CoachingAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingAgreement> = inner.list()
}

fun newCachedCoachingAgreementStore(inner: CoachingAgreementStore, ttl: Int, clock: Clock): CoachingAgreementStore {
    return CachedCoachingAgreementStore(
        inner = inner,
        cache = newTtlCache<CoachingAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching agreements. Empty fields match all. */
data class CoachingAgreementQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingAgreement>): MutableList<CoachingAgreement> {
        val matched = filter(items) { candidate: CoachingAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingAgreements(store: CoachingAgreementStore, query: CoachingAgreementQuery): MutableList<CoachingAgreement> {
    return query.apply(store.list())
}
