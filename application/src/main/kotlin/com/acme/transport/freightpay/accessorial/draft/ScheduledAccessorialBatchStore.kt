package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled accessorial batches by code. */
interface ScheduledAccessorialBatchStore {
    fun find(code: String): ScheduledAccessorialBatch?
    fun save(item: ScheduledAccessorialBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAccessorialBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAccessorialBatchStore(
    var items: MutableMap<String, ScheduledAccessorialBatch>,
    var order: MutableList<String>,
) : ScheduledAccessorialBatchStore {
    override fun find(code: String): ScheduledAccessorialBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAccessorialBatch) {
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

    override fun list(): MutableList<ScheduledAccessorialBatch> {
        val out = mutableListOf<ScheduledAccessorialBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAccessorialBatchStore(): ScheduledAccessorialBatchStore {
    return MemoryScheduledAccessorialBatchStore(
        mutableMapOf<String, ScheduledAccessorialBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAccessorialBatchStore(
    var inner: ScheduledAccessorialBatchStore,
    var cache: TtlCache<ScheduledAccessorialBatch>,
    var hits: Int,
    var misses: Int,
) : ScheduledAccessorialBatchStore {
    override fun find(code: String): ScheduledAccessorialBatch? {
        val hit = (cache.get(code) as ScheduledAccessorialBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAccessorialBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAccessorialBatch> = inner.list()
}

fun newCachedScheduledAccessorialBatchStore(inner: ScheduledAccessorialBatchStore, ttl: Int, clock: Clock): ScheduledAccessorialBatchStore {
    return CachedScheduledAccessorialBatchStore(
        inner = inner,
        cache = newTtlCache<ScheduledAccessorialBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled accessorial batches. Empty fields match all. */
data class ScheduledAccessorialBatchQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAccessorialBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledAccessorialBatch>): MutableList<ScheduledAccessorialBatch> {
        val matched = filter(items) { candidate: ScheduledAccessorialBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAccessorialBatches(store: ScheduledAccessorialBatchStore, query: ScheduledAccessorialBatchQuery): MutableList<ScheduledAccessorialBatch> {
    return query.apply(store.list())
}
