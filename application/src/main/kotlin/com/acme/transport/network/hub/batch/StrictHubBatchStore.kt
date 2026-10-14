package com.acme.transport.network.hub.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict hub batches by code. */
interface StrictHubBatchStore {
    fun find(code: String): StrictHubBatch?
    fun save(item: StrictHubBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictHubBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictHubBatchStore(
    var items: MutableMap<String, StrictHubBatch>,
    var order: MutableList<String>,
) : StrictHubBatchStore {
    override fun find(code: String): StrictHubBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictHubBatch) {
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

    override fun list(): MutableList<StrictHubBatch> {
        val out = mutableListOf<StrictHubBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictHubBatchStore(): StrictHubBatchStore {
    return MemoryStrictHubBatchStore(mutableMapOf<String, StrictHubBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictHubBatchStore(
    var inner: StrictHubBatchStore,
    var cache: TtlCache<StrictHubBatch>,
    var hits: Int,
    var misses: Int,
) : StrictHubBatchStore {
    override fun find(code: String): StrictHubBatch? {
        val hit = (cache.get(code) as StrictHubBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictHubBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictHubBatch> = inner.list()
}

fun newCachedStrictHubBatchStore(inner: StrictHubBatchStore, ttl: Int, clock: Clock): StrictHubBatchStore {
    return CachedStrictHubBatchStore(
        inner = inner,
        cache = newTtlCache<StrictHubBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict hub batches. Empty fields match all. */
data class StrictHubBatchQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictHubBatch): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictHubBatch>): MutableList<StrictHubBatch> {
        val matched = filter(items) { candidate: StrictHubBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictHubBatches(store: StrictHubBatchStore, query: StrictHubBatchQuery): MutableList<StrictHubBatch> {
    return query.apply(store.list())
}
