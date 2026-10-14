package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback scorecard batches by code. */
interface FallbackScorecardBatchStore {
    fun find(code: String): FallbackScorecardBatch?
    fun save(item: FallbackScorecardBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackScorecardBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackScorecardBatchStore(
    var items: MutableMap<String, FallbackScorecardBatch>,
    var order: MutableList<String>,
) : FallbackScorecardBatchStore {
    override fun find(code: String): FallbackScorecardBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackScorecardBatch) {
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

    override fun list(): MutableList<FallbackScorecardBatch> {
        val out = mutableListOf<FallbackScorecardBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackScorecardBatchStore(): FallbackScorecardBatchStore {
    return MemoryFallbackScorecardBatchStore(
        mutableMapOf<String, FallbackScorecardBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackScorecardBatchStore(
    var inner: FallbackScorecardBatchStore,
    var cache: TtlCache<FallbackScorecardBatch>,
    var hits: Int,
    var misses: Int,
) : FallbackScorecardBatchStore {
    override fun find(code: String): FallbackScorecardBatch? {
        val hit = (cache.get(code) as FallbackScorecardBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackScorecardBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackScorecardBatch> = inner.list()
}

fun newCachedFallbackScorecardBatchStore(inner: FallbackScorecardBatchStore, ttl: Int, clock: Clock): FallbackScorecardBatchStore {
    return CachedFallbackScorecardBatchStore(
        inner = inner,
        cache = newTtlCache<FallbackScorecardBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback scorecard batches. Empty fields match all. */
data class FallbackScorecardBatchQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackScorecardBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackScorecardBatch>): MutableList<FallbackScorecardBatch> {
        val matched = filter(items) { candidate: FallbackScorecardBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackScorecardBatches(store: FallbackScorecardBatchStore, query: FallbackScorecardBatchQuery): MutableList<FallbackScorecardBatch> {
    return query.apply(store.list())
}
