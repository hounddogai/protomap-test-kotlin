package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching estimates by code. */
interface CoachingEstimateStore {
    fun find(code: String): CoachingEstimate?
    fun save(item: CoachingEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingEstimateStore(
    var items: MutableMap<String, CoachingEstimate>,
    var order: MutableList<String>,
) : CoachingEstimateStore {
    override fun find(code: String): CoachingEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingEstimate) {
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

    override fun list(): MutableList<CoachingEstimate> {
        val out = mutableListOf<CoachingEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingEstimateStore(): CoachingEstimateStore {
    return MemoryCoachingEstimateStore(mutableMapOf<String, CoachingEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingEstimateStore(
    var inner: CoachingEstimateStore,
    var cache: TtlCache<CoachingEstimate>,
    var hits: Int,
    var misses: Int,
) : CoachingEstimateStore {
    override fun find(code: String): CoachingEstimate? {
        val hit = (cache.get(code) as CoachingEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingEstimate> = inner.list()
}

fun newCachedCoachingEstimateStore(inner: CoachingEstimateStore, ttl: Int, clock: Clock): CoachingEstimateStore {
    return CachedCoachingEstimateStore(
        inner = inner,
        cache = newTtlCache<CoachingEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching estimates. Empty fields match all. */
data class CoachingEstimateQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingEstimate>): MutableList<CoachingEstimate> {
        val matched = filter(items) { candidate: CoachingEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingEstimates(store: CoachingEstimateStore, query: CoachingEstimateQuery): MutableList<CoachingEstimate> {
    return query.apply(store.list())
}
