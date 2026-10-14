package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul batches by code. */
interface BackhaulBatchStore {
    fun find(code: String): BackhaulBatch?
    fun save(item: BackhaulBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulBatchStore(
    var items: MutableMap<String, BackhaulBatch>,
    var order: MutableList<String>,
) : BackhaulBatchStore {
    override fun find(code: String): BackhaulBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulBatch) {
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

    override fun list(): MutableList<BackhaulBatch> {
        val out = mutableListOf<BackhaulBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulBatchStore(): BackhaulBatchStore {
    return MemoryBackhaulBatchStore(mutableMapOf<String, BackhaulBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulBatchStore(
    var inner: BackhaulBatchStore,
    var cache: TtlCache<BackhaulBatch>,
    var hits: Int,
    var misses: Int,
) : BackhaulBatchStore {
    override fun find(code: String): BackhaulBatch? {
        val hit = (cache.get(code) as BackhaulBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulBatch> = inner.list()
}

fun newCachedBackhaulBatchStore(inner: BackhaulBatchStore, ttl: Int, clock: Clock): BackhaulBatchStore {
    return CachedBackhaulBatchStore(
        inner = inner,
        cache = newTtlCache<BackhaulBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul batches. Empty fields match all. */
data class BackhaulBatchQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BackhaulBatch>): MutableList<BackhaulBatch> {
        val matched = filter(items) { candidate: BackhaulBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulBatches(store: BackhaulBatchStore, query: BackhaulBatchQuery): MutableList<BackhaulBatch> {
    return query.apply(store.list())
}
