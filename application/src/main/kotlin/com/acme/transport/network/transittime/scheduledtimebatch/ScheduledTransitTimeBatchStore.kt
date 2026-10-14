package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled transit time batches by code. */
interface ScheduledTransitTimeBatchStore {
    fun find(code: String): ScheduledTransitTimeBatch?
    fun save(item: ScheduledTransitTimeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTransitTimeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTransitTimeBatchStore(
    var items: MutableMap<String, ScheduledTransitTimeBatch>,
    var order: MutableList<String>,
) : ScheduledTransitTimeBatchStore {
    override fun find(code: String): ScheduledTransitTimeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTransitTimeBatch) {
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

    override fun list(): MutableList<ScheduledTransitTimeBatch> {
        val out = mutableListOf<ScheduledTransitTimeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTransitTimeBatchStore(): ScheduledTransitTimeBatchStore {
    return MemoryScheduledTransitTimeBatchStore(
        mutableMapOf<String, ScheduledTransitTimeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTransitTimeBatchStore(
    var inner: ScheduledTransitTimeBatchStore,
    var cache: TtlCache<ScheduledTransitTimeBatch>,
    var hits: Int,
    var misses: Int,
) : ScheduledTransitTimeBatchStore {
    override fun find(code: String): ScheduledTransitTimeBatch? {
        val hit = (cache.get(code) as ScheduledTransitTimeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTransitTimeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTransitTimeBatch> = inner.list()
}

fun newCachedScheduledTransitTimeBatchStore(inner: ScheduledTransitTimeBatchStore, ttl: Int, clock: Clock): ScheduledTransitTimeBatchStore {
    return CachedScheduledTransitTimeBatchStore(
        inner = inner,
        cache = newTtlCache<ScheduledTransitTimeBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled transit time batches. Empty fields match all. */
data class ScheduledTransitTimeBatchQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTransitTimeBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTransitTimeBatch>): MutableList<ScheduledTransitTimeBatch> {
        val matched = filter(items) { candidate: ScheduledTransitTimeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTransitTimeBatches(store: ScheduledTransitTimeBatchStore, query: ScheduledTransitTimeBatchQuery): MutableList<ScheduledTransitTimeBatch> {
    return query.apply(store.list())
}
