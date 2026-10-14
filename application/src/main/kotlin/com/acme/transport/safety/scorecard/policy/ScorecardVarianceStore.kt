package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard variances by code. */
interface ScorecardVarianceStore {
    fun find(code: String): ScorecardVariance?
    fun save(item: ScorecardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardVarianceStore(
    var items: MutableMap<String, ScorecardVariance>,
    var order: MutableList<String>,
) : ScorecardVarianceStore {
    override fun find(code: String): ScorecardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardVariance) {
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

    override fun list(): MutableList<ScorecardVariance> {
        val out = mutableListOf<ScorecardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardVarianceStore(): ScorecardVarianceStore {
    return MemoryScorecardVarianceStore(mutableMapOf<String, ScorecardVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardVarianceStore(
    var inner: ScorecardVarianceStore,
    var cache: TtlCache<ScorecardVariance>,
    var hits: Int,
    var misses: Int,
) : ScorecardVarianceStore {
    override fun find(code: String): ScorecardVariance? {
        val hit = (cache.get(code) as ScorecardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardVariance> = inner.list()
}

fun newCachedScorecardVarianceStore(inner: ScorecardVarianceStore, ttl: Int, clock: Clock): ScorecardVarianceStore {
    return CachedScorecardVarianceStore(
        inner = inner,
        cache = newTtlCache<ScorecardVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard variances. Empty fields match all. */
data class ScorecardVarianceQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardVariance>): MutableList<ScorecardVariance> {
        val matched = filter(items) { candidate: ScorecardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardVariances(store: ScorecardVarianceStore, query: ScorecardVarianceQuery): MutableList<ScorecardVariance> {
    return query.apply(store.list())
}
