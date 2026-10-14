package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard bundles by code. */
interface BatchedScorecardBundleStore {
    fun find(code: String): BatchedScorecardBundle?
    fun save(item: BatchedScorecardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardBundleStore(
    var items: MutableMap<String, BatchedScorecardBundle>,
    var order: MutableList<String>,
) : BatchedScorecardBundleStore {
    override fun find(code: String): BatchedScorecardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardBundle) {
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

    override fun list(): MutableList<BatchedScorecardBundle> {
        val out = mutableListOf<BatchedScorecardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardBundleStore(): BatchedScorecardBundleStore {
    return MemoryBatchedScorecardBundleStore(
        mutableMapOf<String, BatchedScorecardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardBundleStore(
    var inner: BatchedScorecardBundleStore,
    var cache: TtlCache<BatchedScorecardBundle>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardBundleStore {
    override fun find(code: String): BatchedScorecardBundle? {
        val hit = (cache.get(code) as BatchedScorecardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardBundle> = inner.list()
}

fun newCachedBatchedScorecardBundleStore(inner: BatchedScorecardBundleStore, ttl: Int, clock: Clock): BatchedScorecardBundleStore {
    return CachedBatchedScorecardBundleStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard bundles. Empty fields match all. */
data class BatchedScorecardBundleQuery(
    var segment: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardBundle>): MutableList<BatchedScorecardBundle> {
        val matched = filter(items) { candidate: BatchedScorecardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardBundles(store: BatchedScorecardBundleStore, query: BatchedScorecardBundleQuery): MutableList<BatchedScorecardBundle> {
    return query.apply(store.list())
}
