package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching agreements by code. */
interface GlobalCoachingAgreementStore {
    fun find(code: String): GlobalCoachingAgreement?
    fun save(item: GlobalCoachingAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingAgreementStore(
    var items: MutableMap<String, GlobalCoachingAgreement>,
    var order: MutableList<String>,
) : GlobalCoachingAgreementStore {
    override fun find(code: String): GlobalCoachingAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingAgreement) {
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

    override fun list(): MutableList<GlobalCoachingAgreement> {
        val out = mutableListOf<GlobalCoachingAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingAgreementStore(): GlobalCoachingAgreementStore {
    return MemoryGlobalCoachingAgreementStore(
        mutableMapOf<String, GlobalCoachingAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingAgreementStore(
    var inner: GlobalCoachingAgreementStore,
    var cache: TtlCache<GlobalCoachingAgreement>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingAgreementStore {
    override fun find(code: String): GlobalCoachingAgreement? {
        val hit = (cache.get(code) as GlobalCoachingAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingAgreement> = inner.list()
}

fun newCachedGlobalCoachingAgreementStore(inner: GlobalCoachingAgreementStore, ttl: Int, clock: Clock): GlobalCoachingAgreementStore {
    return CachedGlobalCoachingAgreementStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching agreements. Empty fields match all. */
data class GlobalCoachingAgreementQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCoachingAgreement>): MutableList<GlobalCoachingAgreement> {
        val matched = filter(items) { candidate: GlobalCoachingAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingAgreements(store: GlobalCoachingAgreementStore, query: GlobalCoachingAgreementQuery): MutableList<GlobalCoachingAgreement> {
    return query.apply(store.list())
}
