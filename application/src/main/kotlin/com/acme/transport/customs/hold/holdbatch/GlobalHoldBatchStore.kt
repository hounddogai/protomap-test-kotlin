package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global hold batches by code. */
interface GlobalHoldBatchStore {
    fun find(code: String): GlobalHoldBatch?
    fun save(item: GlobalHoldBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalHoldBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalHoldBatchStore(
    var items: MutableMap<String, GlobalHoldBatch>,
    var order: MutableList<String>,
) : GlobalHoldBatchStore {
    override fun find(code: String): GlobalHoldBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalHoldBatch) {
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

    override fun list(): MutableList<GlobalHoldBatch> {
        val out = mutableListOf<GlobalHoldBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalHoldBatchStore(): GlobalHoldBatchStore {
    return MemoryGlobalHoldBatchStore(mutableMapOf<String, GlobalHoldBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalHoldBatchStore(
    var inner: GlobalHoldBatchStore,
    var cache: TtlCache<GlobalHoldBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalHoldBatchStore {
    override fun find(code: String): GlobalHoldBatch? {
        val hit = (cache.get(code) as GlobalHoldBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalHoldBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalHoldBatch> = inner.list()
}

fun newCachedGlobalHoldBatchStore(inner: GlobalHoldBatchStore, ttl: Int, clock: Clock): GlobalHoldBatchStore {
    return CachedGlobalHoldBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalHoldBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global hold batches. Empty fields match all. */
data class GlobalHoldBatchQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalHoldBatch): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalHoldBatch>): MutableList<GlobalHoldBatch> {
        val matched = filter(items) { candidate: GlobalHoldBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalHoldBatches(store: GlobalHoldBatchStore, query: GlobalHoldBatchQuery): MutableList<GlobalHoldBatch> {
    return query.apply(store.list())
}
