package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary cutoff batches by code. */
interface PrimaryCutoffBatchStore {
    fun find(code: String): PrimaryCutoffBatch?
    fun save(item: PrimaryCutoffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCutoffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCutoffBatchStore(
    var items: MutableMap<String, PrimaryCutoffBatch>,
    var order: MutableList<String>,
) : PrimaryCutoffBatchStore {
    override fun find(code: String): PrimaryCutoffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCutoffBatch) {
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

    override fun list(): MutableList<PrimaryCutoffBatch> {
        val out = mutableListOf<PrimaryCutoffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCutoffBatchStore(): PrimaryCutoffBatchStore {
    return MemoryPrimaryCutoffBatchStore(mutableMapOf<String, PrimaryCutoffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCutoffBatchStore(
    var inner: PrimaryCutoffBatchStore,
    var cache: TtlCache<PrimaryCutoffBatch>,
    var hits: Int,
    var misses: Int,
) : PrimaryCutoffBatchStore {
    override fun find(code: String): PrimaryCutoffBatch? {
        val hit = (cache.get(code) as PrimaryCutoffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCutoffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCutoffBatch> = inner.list()
}

fun newCachedPrimaryCutoffBatchStore(inner: PrimaryCutoffBatchStore, ttl: Int, clock: Clock): PrimaryCutoffBatchStore {
    return CachedPrimaryCutoffBatchStore(
        inner = inner,
        cache = newTtlCache<PrimaryCutoffBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary cutoff batches. Empty fields match all. */
data class PrimaryCutoffBatchQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCutoffBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCutoffBatch>): MutableList<PrimaryCutoffBatch> {
        val matched = filter(items) { candidate: PrimaryCutoffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCutoffBatches(store: PrimaryCutoffBatchStore, query: PrimaryCutoffBatchQuery): MutableList<PrimaryCutoffBatch> {
    return query.apply(store.list())
}
