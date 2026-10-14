package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual transit time batches by code. */
interface ManualTransitTimeBatchStore {
    fun find(code: String): ManualTransitTimeBatch?
    fun save(item: ManualTransitTimeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTransitTimeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTransitTimeBatchStore(
    var items: MutableMap<String, ManualTransitTimeBatch>,
    var order: MutableList<String>,
) : ManualTransitTimeBatchStore {
    override fun find(code: String): ManualTransitTimeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTransitTimeBatch) {
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

    override fun list(): MutableList<ManualTransitTimeBatch> {
        val out = mutableListOf<ManualTransitTimeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTransitTimeBatchStore(): ManualTransitTimeBatchStore {
    return MemoryManualTransitTimeBatchStore(
        mutableMapOf<String, ManualTransitTimeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTransitTimeBatchStore(
    var inner: ManualTransitTimeBatchStore,
    var cache: TtlCache<ManualTransitTimeBatch>,
    var hits: Int,
    var misses: Int,
) : ManualTransitTimeBatchStore {
    override fun find(code: String): ManualTransitTimeBatch? {
        val hit = (cache.get(code) as ManualTransitTimeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTransitTimeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTransitTimeBatch> = inner.list()
}

fun newCachedManualTransitTimeBatchStore(inner: ManualTransitTimeBatchStore, ttl: Int, clock: Clock): ManualTransitTimeBatchStore {
    return CachedManualTransitTimeBatchStore(
        inner = inner,
        cache = newTtlCache<ManualTransitTimeBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual transit time batches. Empty fields match all. */
data class ManualTransitTimeBatchQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTransitTimeBatch): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTransitTimeBatch>): MutableList<ManualTransitTimeBatch> {
        val matched = filter(items) { candidate: ManualTransitTimeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTransitTimeBatches(store: ManualTransitTimeBatchStore, query: ManualTransitTimeBatchQuery): MutableList<ManualTransitTimeBatch> {
    return query.apply(store.list())
}
