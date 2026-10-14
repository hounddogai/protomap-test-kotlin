package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff batches by code. */
interface CutoffBatchStore {
    fun find(code: String): CutoffBatch?
    fun save(item: CutoffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffBatchStore(
    var items: MutableMap<String, CutoffBatch>,
    var order: MutableList<String>,
) : CutoffBatchStore {
    override fun find(code: String): CutoffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffBatch) {
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

    override fun list(): MutableList<CutoffBatch> {
        val out = mutableListOf<CutoffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffBatchStore(): CutoffBatchStore {
    return MemoryCutoffBatchStore(mutableMapOf<String, CutoffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffBatchStore(
    var inner: CutoffBatchStore,
    var cache: TtlCache<CutoffBatch>,
    var hits: Int,
    var misses: Int,
) : CutoffBatchStore {
    override fun find(code: String): CutoffBatch? {
        val hit = (cache.get(code) as CutoffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffBatch> = inner.list()
}

fun newCachedCutoffBatchStore(inner: CutoffBatchStore, ttl: Int, clock: Clock): CutoffBatchStore {
    return CachedCutoffBatchStore(
        inner = inner,
        cache = newTtlCache<CutoffBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff batches. Empty fields match all. */
data class CutoffBatchQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CutoffBatch>): MutableList<CutoffBatch> {
        val matched = filter(items) { candidate: CutoffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffBatches(store: CutoffBatchStore, query: CutoffBatchQuery): MutableList<CutoffBatch> {
    return query.apply(store.list())
}
