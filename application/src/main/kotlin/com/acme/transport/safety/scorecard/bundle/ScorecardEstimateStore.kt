package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard estimates by code. */
interface ScorecardEstimateStore {
    fun find(code: String): ScorecardEstimate?
    fun save(item: ScorecardEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardEstimateStore(
    var items: MutableMap<String, ScorecardEstimate>,
    var order: MutableList<String>,
) : ScorecardEstimateStore {
    override fun find(code: String): ScorecardEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardEstimate) {
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

    override fun list(): MutableList<ScorecardEstimate> {
        val out = mutableListOf<ScorecardEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardEstimateStore(): ScorecardEstimateStore {
    return MemoryScorecardEstimateStore(mutableMapOf<String, ScorecardEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardEstimateStore(
    var inner: ScorecardEstimateStore,
    var cache: TtlCache<ScorecardEstimate>,
    var hits: Int,
    var misses: Int,
) : ScorecardEstimateStore {
    override fun find(code: String): ScorecardEstimate? {
        val hit = (cache.get(code) as ScorecardEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardEstimate> = inner.list()
}

fun newCachedScorecardEstimateStore(inner: ScorecardEstimateStore, ttl: Int, clock: Clock): ScorecardEstimateStore {
    return CachedScorecardEstimateStore(
        inner = inner,
        cache = newTtlCache<ScorecardEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard estimates. Empty fields match all. */
data class ScorecardEstimateQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardEstimate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScorecardEstimate>): MutableList<ScorecardEstimate> {
        val matched = filter(items) { candidate: ScorecardEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardEstimates(store: ScorecardEstimateStore, query: ScorecardEstimateQuery): MutableList<ScorecardEstimate> {
    return query.apply(store.list())
}
