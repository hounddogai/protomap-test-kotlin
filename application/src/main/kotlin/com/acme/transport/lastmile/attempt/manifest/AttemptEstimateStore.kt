package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt estimates by code. */
interface AttemptEstimateStore {
    fun find(code: String): AttemptEstimate?
    fun save(item: AttemptEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptEstimateStore(
    var items: MutableMap<String, AttemptEstimate>,
    var order: MutableList<String>,
) : AttemptEstimateStore {
    override fun find(code: String): AttemptEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptEstimate) {
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

    override fun list(): MutableList<AttemptEstimate> {
        val out = mutableListOf<AttemptEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptEstimateStore(): AttemptEstimateStore {
    return MemoryAttemptEstimateStore(mutableMapOf<String, AttemptEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptEstimateStore(
    var inner: AttemptEstimateStore,
    var cache: TtlCache<AttemptEstimate>,
    var hits: Int,
    var misses: Int,
) : AttemptEstimateStore {
    override fun find(code: String): AttemptEstimate? {
        val hit = (cache.get(code) as AttemptEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptEstimate> = inner.list()
}

fun newCachedAttemptEstimateStore(inner: AttemptEstimateStore, ttl: Int, clock: Clock): AttemptEstimateStore {
    return CachedAttemptEstimateStore(
        inner = inner,
        cache = newTtlCache<AttemptEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt estimates. Empty fields match all. */
data class AttemptEstimateQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AttemptEstimate>): MutableList<AttemptEstimate> {
        val matched = filter(items) { candidate: AttemptEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptEstimates(store: AttemptEstimateStore, query: AttemptEstimateQuery): MutableList<AttemptEstimate> {
    return query.apply(store.list())
}
