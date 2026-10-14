package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt batches by code. */
interface AttemptBatchStore {
    fun find(code: String): AttemptBatch?
    fun save(item: AttemptBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptBatchStore(
    var items: MutableMap<String, AttemptBatch>,
    var order: MutableList<String>,
) : AttemptBatchStore {
    override fun find(code: String): AttemptBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptBatch) {
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

    override fun list(): MutableList<AttemptBatch> {
        val out = mutableListOf<AttemptBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptBatchStore(): AttemptBatchStore {
    return MemoryAttemptBatchStore(mutableMapOf<String, AttemptBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptBatchStore(
    var inner: AttemptBatchStore,
    var cache: TtlCache<AttemptBatch>,
    var hits: Int,
    var misses: Int,
) : AttemptBatchStore {
    override fun find(code: String): AttemptBatch? {
        val hit = (cache.get(code) as AttemptBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptBatch> = inner.list()
}

fun newCachedAttemptBatchStore(inner: AttemptBatchStore, ttl: Int, clock: Clock): AttemptBatchStore {
    return CachedAttemptBatchStore(
        inner = inner,
        cache = newTtlCache<AttemptBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt batches. Empty fields match all. */
data class AttemptBatchQuery(
    var country: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptBatch>): MutableList<AttemptBatch> {
        val matched = filter(items) { candidate: AttemptBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptBatches(store: AttemptBatchStore, query: AttemptBatchQuery): MutableList<AttemptBatch> {
    return query.apply(store.list())
}
