package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled tender batches by code. */
interface ScheduledTenderBatchStore {
    fun find(code: String): ScheduledTenderBatch?
    fun save(item: ScheduledTenderBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTenderBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTenderBatchStore(
    var items: MutableMap<String, ScheduledTenderBatch>,
    var order: MutableList<String>,
) : ScheduledTenderBatchStore {
    override fun find(code: String): ScheduledTenderBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTenderBatch) {
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

    override fun list(): MutableList<ScheduledTenderBatch> {
        val out = mutableListOf<ScheduledTenderBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTenderBatchStore(): ScheduledTenderBatchStore {
    return MemoryScheduledTenderBatchStore(
        mutableMapOf<String, ScheduledTenderBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTenderBatchStore(
    var inner: ScheduledTenderBatchStore,
    var cache: TtlCache<ScheduledTenderBatch>,
    var hits: Int,
    var misses: Int,
) : ScheduledTenderBatchStore {
    override fun find(code: String): ScheduledTenderBatch? {
        val hit = (cache.get(code) as ScheduledTenderBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTenderBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTenderBatch> = inner.list()
}

fun newCachedScheduledTenderBatchStore(inner: ScheduledTenderBatchStore, ttl: Int, clock: Clock): ScheduledTenderBatchStore {
    return CachedScheduledTenderBatchStore(
        inner = inner,
        cache = newTtlCache<ScheduledTenderBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled tender batches. Empty fields match all. */
data class ScheduledTenderBatchQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTenderBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTenderBatch>): MutableList<ScheduledTenderBatch> {
        val matched = filter(items) { candidate: ScheduledTenderBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTenderBatches(store: ScheduledTenderBatchStore, query: ScheduledTenderBatchQuery): MutableList<ScheduledTenderBatch> {
    return query.apply(store.list())
}
