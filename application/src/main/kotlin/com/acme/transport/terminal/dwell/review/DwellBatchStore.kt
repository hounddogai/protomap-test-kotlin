package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell batches by code. */
interface DwellBatchStore {
    fun find(code: String): DwellBatch?
    fun save(item: DwellBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellBatchStore(
    var items: MutableMap<String, DwellBatch>,
    var order: MutableList<String>,
) : DwellBatchStore {
    override fun find(code: String): DwellBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellBatch) {
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

    override fun list(): MutableList<DwellBatch> {
        val out = mutableListOf<DwellBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellBatchStore(): DwellBatchStore {
    return MemoryDwellBatchStore(mutableMapOf<String, DwellBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellBatchStore(
    var inner: DwellBatchStore,
    var cache: TtlCache<DwellBatch>,
    var hits: Int,
    var misses: Int,
) : DwellBatchStore {
    override fun find(code: String): DwellBatch? {
        val hit = (cache.get(code) as DwellBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellBatch> = inner.list()
}

fun newCachedDwellBatchStore(inner: DwellBatchStore, ttl: Int, clock: Clock): DwellBatchStore {
    return CachedDwellBatchStore(
        inner = inner,
        cache = newTtlCache<DwellBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell batches. Empty fields match all. */
data class DwellBatchQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellBatch): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellBatch>): MutableList<DwellBatch> {
        val matched = filter(items) { candidate: DwellBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellBatches(store: DwellBatchStore, query: DwellBatchQuery): MutableList<DwellBatch> {
    return query.apply(store.list())
}
