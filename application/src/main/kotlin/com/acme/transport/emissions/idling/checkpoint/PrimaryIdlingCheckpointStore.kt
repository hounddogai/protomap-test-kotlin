package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary idling checkpoints by code. */
interface PrimaryIdlingCheckpointStore {
    fun find(code: String): PrimaryIdlingCheckpoint?
    fun save(item: PrimaryIdlingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIdlingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIdlingCheckpointStore(
    var items: MutableMap<String, PrimaryIdlingCheckpoint>,
    var order: MutableList<String>,
) : PrimaryIdlingCheckpointStore {
    override fun find(code: String): PrimaryIdlingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIdlingCheckpoint) {
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

    override fun list(): MutableList<PrimaryIdlingCheckpoint> {
        val out = mutableListOf<PrimaryIdlingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIdlingCheckpointStore(): PrimaryIdlingCheckpointStore {
    return MemoryPrimaryIdlingCheckpointStore(
        mutableMapOf<String, PrimaryIdlingCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIdlingCheckpointStore(
    var inner: PrimaryIdlingCheckpointStore,
    var cache: TtlCache<PrimaryIdlingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PrimaryIdlingCheckpointStore {
    override fun find(code: String): PrimaryIdlingCheckpoint? {
        val hit = (cache.get(code) as PrimaryIdlingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIdlingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIdlingCheckpoint> = inner.list()
}

fun newCachedPrimaryIdlingCheckpointStore(inner: PrimaryIdlingCheckpointStore, ttl: Int, clock: Clock): PrimaryIdlingCheckpointStore {
    return CachedPrimaryIdlingCheckpointStore(
        inner = inner,
        cache = newTtlCache<PrimaryIdlingCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary idling checkpoints. Empty fields match all. */
data class PrimaryIdlingCheckpointQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIdlingCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryIdlingCheckpoint>): MutableList<PrimaryIdlingCheckpoint> {
        val matched = filter(items) { candidate: PrimaryIdlingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIdlingCheckpoints(store: PrimaryIdlingCheckpointStore, query: PrimaryIdlingCheckpointQuery): MutableList<PrimaryIdlingCheckpoint> {
    return query.apply(store.list())
}
