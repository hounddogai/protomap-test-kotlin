package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global violation batches by code. */
interface GlobalViolationBatchStore {
    fun find(code: String): GlobalViolationBatch?
    fun save(item: GlobalViolationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalViolationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalViolationBatchStore(
    var items: MutableMap<String, GlobalViolationBatch>,
    var order: MutableList<String>,
) : GlobalViolationBatchStore {
    override fun find(code: String): GlobalViolationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalViolationBatch) {
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

    override fun list(): MutableList<GlobalViolationBatch> {
        val out = mutableListOf<GlobalViolationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalViolationBatchStore(): GlobalViolationBatchStore {
    return MemoryGlobalViolationBatchStore(
        mutableMapOf<String, GlobalViolationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalViolationBatchStore(
    var inner: GlobalViolationBatchStore,
    var cache: TtlCache<GlobalViolationBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalViolationBatchStore {
    override fun find(code: String): GlobalViolationBatch? {
        val hit = (cache.get(code) as GlobalViolationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalViolationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalViolationBatch> = inner.list()
}

fun newCachedGlobalViolationBatchStore(inner: GlobalViolationBatchStore, ttl: Int, clock: Clock): GlobalViolationBatchStore {
    return CachedGlobalViolationBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalViolationBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global violation batches. Empty fields match all. */
data class GlobalViolationBatchQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalViolationBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalViolationBatch>): MutableList<GlobalViolationBatch> {
        val matched = filter(items) { candidate: GlobalViolationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalViolationBatches(store: GlobalViolationBatchStore, query: GlobalViolationBatchQuery): MutableList<GlobalViolationBatch> {
    return query.apply(store.list())
}
