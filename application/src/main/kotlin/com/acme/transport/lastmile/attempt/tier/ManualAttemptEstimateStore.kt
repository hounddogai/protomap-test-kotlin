package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual attempt estimates by code. */
interface ManualAttemptEstimateStore {
    fun find(code: String): ManualAttemptEstimate?
    fun save(item: ManualAttemptEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAttemptEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAttemptEstimateStore(
    var items: MutableMap<String, ManualAttemptEstimate>,
    var order: MutableList<String>,
) : ManualAttemptEstimateStore {
    override fun find(code: String): ManualAttemptEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAttemptEstimate) {
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

    override fun list(): MutableList<ManualAttemptEstimate> {
        val out = mutableListOf<ManualAttemptEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAttemptEstimateStore(): ManualAttemptEstimateStore {
    return MemoryManualAttemptEstimateStore(
        mutableMapOf<String, ManualAttemptEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAttemptEstimateStore(
    var inner: ManualAttemptEstimateStore,
    var cache: TtlCache<ManualAttemptEstimate>,
    var hits: Int,
    var misses: Int,
) : ManualAttemptEstimateStore {
    override fun find(code: String): ManualAttemptEstimate? {
        val hit = (cache.get(code) as ManualAttemptEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAttemptEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAttemptEstimate> = inner.list()
}

fun newCachedManualAttemptEstimateStore(inner: ManualAttemptEstimateStore, ttl: Int, clock: Clock): ManualAttemptEstimateStore {
    return CachedManualAttemptEstimateStore(
        inner = inner,
        cache = newTtlCache<ManualAttemptEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual attempt estimates. Empty fields match all. */
data class ManualAttemptEstimateQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAttemptEstimate): Boolean {
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

    fun apply(items: MutableList<ManualAttemptEstimate>): MutableList<ManualAttemptEstimate> {
        val matched = filter(items) { candidate: ManualAttemptEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAttemptEstimates(store: ManualAttemptEstimateStore, query: ManualAttemptEstimateQuery): MutableList<ManualAttemptEstimate> {
    return query.apply(store.list())
}
