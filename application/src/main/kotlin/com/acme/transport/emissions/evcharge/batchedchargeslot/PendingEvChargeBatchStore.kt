package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending ev charge batches by code. */
interface PendingEvChargeBatchStore {
    fun find(code: String): PendingEvChargeBatch?
    fun save(item: PendingEvChargeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEvChargeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEvChargeBatchStore(
    var items: MutableMap<String, PendingEvChargeBatch>,
    var order: MutableList<String>,
) : PendingEvChargeBatchStore {
    override fun find(code: String): PendingEvChargeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEvChargeBatch) {
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

    override fun list(): MutableList<PendingEvChargeBatch> {
        val out = mutableListOf<PendingEvChargeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEvChargeBatchStore(): PendingEvChargeBatchStore {
    return MemoryPendingEvChargeBatchStore(
        mutableMapOf<String, PendingEvChargeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEvChargeBatchStore(
    var inner: PendingEvChargeBatchStore,
    var cache: TtlCache<PendingEvChargeBatch>,
    var hits: Int,
    var misses: Int,
) : PendingEvChargeBatchStore {
    override fun find(code: String): PendingEvChargeBatch? {
        val hit = (cache.get(code) as PendingEvChargeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEvChargeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEvChargeBatch> = inner.list()
}

fun newCachedPendingEvChargeBatchStore(inner: PendingEvChargeBatchStore, ttl: Int, clock: Clock): PendingEvChargeBatchStore {
    return CachedPendingEvChargeBatchStore(
        inner = inner,
        cache = newTtlCache<PendingEvChargeBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending ev charge batches. Empty fields match all. */
data class PendingEvChargeBatchQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEvChargeBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PendingEvChargeBatch>): MutableList<PendingEvChargeBatch> {
        val matched = filter(items) { candidate: PendingEvChargeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEvChargeBatches(store: PendingEvChargeBatchStore, query: PendingEvChargeBatchQuery): MutableList<PendingEvChargeBatch> {
    return query.apply(store.list())
}
