package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal near miss agreements by code. */
interface SeasonalNearMissAgreementStore {
    fun find(code: String): SeasonalNearMissAgreement?
    fun save(item: SeasonalNearMissAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalNearMissAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalNearMissAgreementStore(
    var items: MutableMap<String, SeasonalNearMissAgreement>,
    var order: MutableList<String>,
) : SeasonalNearMissAgreementStore {
    override fun find(code: String): SeasonalNearMissAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalNearMissAgreement) {
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

    override fun list(): MutableList<SeasonalNearMissAgreement> {
        val out = mutableListOf<SeasonalNearMissAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalNearMissAgreementStore(): SeasonalNearMissAgreementStore {
    return MemorySeasonalNearMissAgreementStore(
        mutableMapOf<String, SeasonalNearMissAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalNearMissAgreementStore(
    var inner: SeasonalNearMissAgreementStore,
    var cache: TtlCache<SeasonalNearMissAgreement>,
    var hits: Int,
    var misses: Int,
) : SeasonalNearMissAgreementStore {
    override fun find(code: String): SeasonalNearMissAgreement? {
        val hit = (cache.get(code) as SeasonalNearMissAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalNearMissAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalNearMissAgreement> = inner.list()
}

fun newCachedSeasonalNearMissAgreementStore(inner: SeasonalNearMissAgreementStore, ttl: Int, clock: Clock): SeasonalNearMissAgreementStore {
    return CachedSeasonalNearMissAgreementStore(
        inner = inner,
        cache = newTtlCache<SeasonalNearMissAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal near miss agreements. Empty fields match all. */
data class SeasonalNearMissAgreementQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalNearMissAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SeasonalNearMissAgreement>): MutableList<SeasonalNearMissAgreement> {
        val matched = filter(items) { candidate: SeasonalNearMissAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalNearMissAgreements(store: SeasonalNearMissAgreementStore, query: SeasonalNearMissAgreementQuery): MutableList<SeasonalNearMissAgreement> {
    return query.apply(store.list())
}
