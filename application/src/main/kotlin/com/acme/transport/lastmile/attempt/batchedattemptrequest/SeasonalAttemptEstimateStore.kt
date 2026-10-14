package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal attempt estimates by code. */
interface SeasonalAttemptEstimateStore {
    fun find(code: String): SeasonalAttemptEstimate?
    fun save(item: SeasonalAttemptEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAttemptEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAttemptEstimateStore(
    var items: MutableMap<String, SeasonalAttemptEstimate>,
    var order: MutableList<String>,
) : SeasonalAttemptEstimateStore {
    override fun find(code: String): SeasonalAttemptEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAttemptEstimate) {
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

    override fun list(): MutableList<SeasonalAttemptEstimate> {
        val out = mutableListOf<SeasonalAttemptEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAttemptEstimateStore(): SeasonalAttemptEstimateStore {
    return MemorySeasonalAttemptEstimateStore(
        mutableMapOf<String, SeasonalAttemptEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAttemptEstimateStore(
    var inner: SeasonalAttemptEstimateStore,
    var cache: TtlCache<SeasonalAttemptEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalAttemptEstimateStore {
    override fun find(code: String): SeasonalAttemptEstimate? {
        val hit = (cache.get(code) as SeasonalAttemptEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAttemptEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAttemptEstimate> = inner.list()
}

fun newCachedSeasonalAttemptEstimateStore(inner: SeasonalAttemptEstimateStore, ttl: Int, clock: Clock): SeasonalAttemptEstimateStore {
    return CachedSeasonalAttemptEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalAttemptEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal attempt estimates. Empty fields match all. */
data class SeasonalAttemptEstimateQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAttemptEstimate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAttemptEstimate>): MutableList<SeasonalAttemptEstimate> {
        val matched = filter(items) { candidate: SeasonalAttemptEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAttemptEstimates(store: SeasonalAttemptEstimateStore, query: SeasonalAttemptEstimateQuery): MutableList<SeasonalAttemptEstimate> {
    return query.apply(store.list())
}
