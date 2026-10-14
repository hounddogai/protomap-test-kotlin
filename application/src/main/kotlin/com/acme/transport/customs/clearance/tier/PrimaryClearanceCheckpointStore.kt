package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary clearance checkpoints by code. */
interface PrimaryClearanceCheckpointStore {
    fun find(code: String): PrimaryClearanceCheckpoint?
    fun save(item: PrimaryClearanceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryClearanceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryClearanceCheckpointStore(
    var items: MutableMap<String, PrimaryClearanceCheckpoint>,
    var order: MutableList<String>,
) : PrimaryClearanceCheckpointStore {
    override fun find(code: String): PrimaryClearanceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryClearanceCheckpoint) {
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

    override fun list(): MutableList<PrimaryClearanceCheckpoint> {
        val out = mutableListOf<PrimaryClearanceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryClearanceCheckpointStore(): PrimaryClearanceCheckpointStore {
    return MemoryPrimaryClearanceCheckpointStore(
        mutableMapOf<String, PrimaryClearanceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryClearanceCheckpointStore(
    var inner: PrimaryClearanceCheckpointStore,
    var cache: TtlCache<PrimaryClearanceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PrimaryClearanceCheckpointStore {
    override fun find(code: String): PrimaryClearanceCheckpoint? {
        val hit = (cache.get(code) as PrimaryClearanceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryClearanceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryClearanceCheckpoint> = inner.list()
}

fun newCachedPrimaryClearanceCheckpointStore(inner: PrimaryClearanceCheckpointStore, ttl: Int, clock: Clock): PrimaryClearanceCheckpointStore {
    return CachedPrimaryClearanceCheckpointStore(
        inner = inner,
        cache = newTtlCache<PrimaryClearanceCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary clearance checkpoints. Empty fields match all. */
data class PrimaryClearanceCheckpointQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryClearanceCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryClearanceCheckpoint>): MutableList<PrimaryClearanceCheckpoint> {
        val matched = filter(items) { candidate: PrimaryClearanceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryClearanceCheckpoints(store: PrimaryClearanceCheckpointStore, query: PrimaryClearanceCheckpointQuery): MutableList<PrimaryClearanceCheckpoint> {
    return query.apply(store.list())
}
