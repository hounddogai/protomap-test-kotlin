package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop batches by code. */
interface DropBatchStore {
    fun find(code: String): DropBatch?
    fun save(item: DropBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropBatchStore(
    var items: MutableMap<String, DropBatch>,
    var order: MutableList<String>,
) : DropBatchStore {
    override fun find(code: String): DropBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropBatch) {
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

    override fun list(): MutableList<DropBatch> {
        val out = mutableListOf<DropBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropBatchStore(): DropBatchStore {
    return MemoryDropBatchStore(mutableMapOf<String, DropBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropBatchStore(
    var inner: DropBatchStore,
    var cache: TtlCache<DropBatch>,
    var hits: Int,
    var misses: Int,
) : DropBatchStore {
    override fun find(code: String): DropBatch? {
        val hit = (cache.get(code) as DropBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropBatch> = inner.list()
}

fun newCachedDropBatchStore(inner: DropBatchStore, ttl: Int, clock: Clock): DropBatchStore {
    return CachedDropBatchStore(
        inner = inner,
        cache = newTtlCache<DropBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop batches. Empty fields match all. */
data class DropBatchQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropBatch>): MutableList<DropBatch> {
        val matched = filter(items) { candidate: DropBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropBatches(store: DropBatchStore, query: DropBatchQuery): MutableList<DropBatch> {
    return query.apply(store.list())
}
