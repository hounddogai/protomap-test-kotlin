package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul batches by code. */
interface FallbackBackhaulBatchStore {
    fun find(code: String): FallbackBackhaulBatch?
    fun save(item: FallbackBackhaulBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulBatchStore(
    var items: MutableMap<String, FallbackBackhaulBatch>,
    var order: MutableList<String>,
) : FallbackBackhaulBatchStore {
    override fun find(code: String): FallbackBackhaulBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulBatch) {
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

    override fun list(): MutableList<FallbackBackhaulBatch> {
        val out = mutableListOf<FallbackBackhaulBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulBatchStore(): FallbackBackhaulBatchStore {
    return MemoryFallbackBackhaulBatchStore(
        mutableMapOf<String, FallbackBackhaulBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulBatchStore(
    var inner: FallbackBackhaulBatchStore,
    var cache: TtlCache<FallbackBackhaulBatch>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulBatchStore {
    override fun find(code: String): FallbackBackhaulBatch? {
        val hit = (cache.get(code) as FallbackBackhaulBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulBatch> = inner.list()
}

fun newCachedFallbackBackhaulBatchStore(inner: FallbackBackhaulBatchStore, ttl: Int, clock: Clock): FallbackBackhaulBatchStore {
    return CachedFallbackBackhaulBatchStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul batches. Empty fields match all. */
data class FallbackBackhaulBatchQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulBatch>): MutableList<FallbackBackhaulBatch> {
        val matched = filter(items) { candidate: FallbackBackhaulBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulBatches(store: FallbackBackhaulBatchStore, query: FallbackBackhaulBatchQuery): MutableList<FallbackBackhaulBatch> {
    return query.apply(store.list())
}
