package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire batches by code. */
interface TireBatchStore {
    fun find(code: String): TireBatch?
    fun save(item: TireBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireBatchStore(
    var items: MutableMap<String, TireBatch>,
    var order: MutableList<String>,
) : TireBatchStore {
    override fun find(code: String): TireBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireBatch) {
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

    override fun list(): MutableList<TireBatch> {
        val out = mutableListOf<TireBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireBatchStore(): TireBatchStore {
    return MemoryTireBatchStore(mutableMapOf<String, TireBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireBatchStore(
    var inner: TireBatchStore,
    var cache: TtlCache<TireBatch>,
    var hits: Int,
    var misses: Int,
) : TireBatchStore {
    override fun find(code: String): TireBatch? {
        val hit = (cache.get(code) as TireBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireBatch> = inner.list()
}

fun newCachedTireBatchStore(inner: TireBatchStore, ttl: Int, clock: Clock): TireBatchStore {
    return CachedTireBatchStore(
        inner = inner,
        cache = newTtlCache<TireBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire batches. Empty fields match all. */
data class TireBatchQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TireBatch>): MutableList<TireBatch> {
        val matched = filter(items) { candidate: TireBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireBatches(store: TireBatchStore, query: TireBatchQuery): MutableList<TireBatch> {
    return query.apply(store.list())
}
