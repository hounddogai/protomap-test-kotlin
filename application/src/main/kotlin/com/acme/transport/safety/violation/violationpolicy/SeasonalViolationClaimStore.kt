package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal violation claims by code. */
interface SeasonalViolationClaimStore {
    fun find(code: String): SeasonalViolationClaim?
    fun save(item: SeasonalViolationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalViolationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalViolationClaimStore(
    var items: MutableMap<String, SeasonalViolationClaim>,
    var order: MutableList<String>,
) : SeasonalViolationClaimStore {
    override fun find(code: String): SeasonalViolationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalViolationClaim) {
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

    override fun list(): MutableList<SeasonalViolationClaim> {
        val out = mutableListOf<SeasonalViolationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalViolationClaimStore(): SeasonalViolationClaimStore {
    return MemorySeasonalViolationClaimStore(
        mutableMapOf<String, SeasonalViolationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalViolationClaimStore(
    var inner: SeasonalViolationClaimStore,
    var cache: TtlCache<SeasonalViolationClaim>,
    var hits: Int,
    var misses: Int,
) : SeasonalViolationClaimStore {
    override fun find(code: String): SeasonalViolationClaim? {
        val hit = (cache.get(code) as SeasonalViolationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalViolationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalViolationClaim> = inner.list()
}

fun newCachedSeasonalViolationClaimStore(inner: SeasonalViolationClaimStore, ttl: Int, clock: Clock): SeasonalViolationClaimStore {
    return CachedSeasonalViolationClaimStore(
        inner = inner,
        cache = newTtlCache<SeasonalViolationClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal violation claims. Empty fields match all. */
data class SeasonalViolationClaimQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalViolationClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalViolationClaim>): MutableList<SeasonalViolationClaim> {
        val matched = filter(items) { candidate: SeasonalViolationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalViolationClaims(store: SeasonalViolationClaimStore, query: SeasonalViolationClaimQuery): MutableList<SeasonalViolationClaim> {
    return query.apply(store.list())
}
