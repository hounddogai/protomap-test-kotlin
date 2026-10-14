package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard batches by code. */
interface ScorecardBatchStore {
    fun find(code: String): ScorecardBatch?
    fun save(item: ScorecardBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardBatchStore(
    var items: MutableMap<String, ScorecardBatch>,
    var order: MutableList<String>,
) : ScorecardBatchStore {
    override fun find(code: String): ScorecardBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardBatch) {
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

    override fun list(): MutableList<ScorecardBatch> {
        val out = mutableListOf<ScorecardBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardBatchStore(): ScorecardBatchStore {
    return MemoryScorecardBatchStore(mutableMapOf<String, ScorecardBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardBatchStore(
    var inner: ScorecardBatchStore,
    var cache: TtlCache<ScorecardBatch>,
    var hits: Int,
    var misses: Int,
) : ScorecardBatchStore {
    override fun find(code: String): ScorecardBatch? {
        val hit = (cache.get(code) as ScorecardBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardBatch> = inner.list()
}

fun newCachedScorecardBatchStore(inner: ScorecardBatchStore, ttl: Int, clock: Clock): ScorecardBatchStore {
    return CachedScorecardBatchStore(
        inner = inner,
        cache = newTtlCache<ScorecardBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard batches. Empty fields match all. */
data class ScorecardBatchQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardBatch>): MutableList<ScorecardBatch> {
        val matched = filter(items) { candidate: ScorecardBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardBatches(store: ScorecardBatchStore, query: ScorecardBatchQuery): MutableList<ScorecardBatch> {
    return query.apply(store.list())
}
