package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time batches by code. */
interface TransitTimeBatchStore {
    fun find(code: String): TransitTimeBatch?
    fun save(item: TransitTimeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeBatchStore(
    var items: MutableMap<String, TransitTimeBatch>,
    var order: MutableList<String>,
) : TransitTimeBatchStore {
    override fun find(code: String): TransitTimeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeBatch) {
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

    override fun list(): MutableList<TransitTimeBatch> {
        val out = mutableListOf<TransitTimeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeBatchStore(): TransitTimeBatchStore {
    return MemoryTransitTimeBatchStore(mutableMapOf<String, TransitTimeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeBatchStore(
    var inner: TransitTimeBatchStore,
    var cache: TtlCache<TransitTimeBatch>,
    var hits: Int,
    var misses: Int,
) : TransitTimeBatchStore {
    override fun find(code: String): TransitTimeBatch? {
        val hit = (cache.get(code) as TransitTimeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeBatch> = inner.list()
}

fun newCachedTransitTimeBatchStore(inner: TransitTimeBatchStore, ttl: Int, clock: Clock): TransitTimeBatchStore {
    return CachedTransitTimeBatchStore(
        inner = inner,
        cache = newTtlCache<TransitTimeBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time batches. Empty fields match all. */
data class TransitTimeBatchQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeBatch>): MutableList<TransitTimeBatch> {
        val matched = filter(items) { candidate: TransitTimeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeBatches(store: TransitTimeBatchStore, query: TransitTimeBatchQuery): MutableList<TransitTimeBatch> {
    return query.apply(store.list())
}
