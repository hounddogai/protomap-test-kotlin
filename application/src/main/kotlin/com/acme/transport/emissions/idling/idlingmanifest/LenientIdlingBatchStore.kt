package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient idling batches by code. */
interface LenientIdlingBatchStore {
    fun find(code: String): LenientIdlingBatch?
    fun save(item: LenientIdlingBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIdlingBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIdlingBatchStore(
    var items: MutableMap<String, LenientIdlingBatch>,
    var order: MutableList<String>,
) : LenientIdlingBatchStore {
    override fun find(code: String): LenientIdlingBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIdlingBatch) {
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

    override fun list(): MutableList<LenientIdlingBatch> {
        val out = mutableListOf<LenientIdlingBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIdlingBatchStore(): LenientIdlingBatchStore {
    return MemoryLenientIdlingBatchStore(mutableMapOf<String, LenientIdlingBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIdlingBatchStore(
    var inner: LenientIdlingBatchStore,
    var cache: TtlCache<LenientIdlingBatch>,
    var hits: Int,
    var misses: Int,
) : LenientIdlingBatchStore {
    override fun find(code: String): LenientIdlingBatch? {
        val hit = (cache.get(code) as LenientIdlingBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIdlingBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIdlingBatch> = inner.list()
}

fun newCachedLenientIdlingBatchStore(inner: LenientIdlingBatchStore, ttl: Int, clock: Clock): LenientIdlingBatchStore {
    return CachedLenientIdlingBatchStore(
        inner = inner,
        cache = newTtlCache<LenientIdlingBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient idling batches. Empty fields match all. */
data class LenientIdlingBatchQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIdlingBatch): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<LenientIdlingBatch>): MutableList<LenientIdlingBatch> {
        val matched = filter(items) { candidate: LenientIdlingBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIdlingBatches(store: LenientIdlingBatchStore, query: LenientIdlingBatchQuery): MutableList<LenientIdlingBatch> {
    return query.apply(store.list())
}
