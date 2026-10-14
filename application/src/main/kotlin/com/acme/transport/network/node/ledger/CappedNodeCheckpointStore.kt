package com.acme.transport.network.node.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped node checkpoints by code. */
interface CappedNodeCheckpointStore {
    fun find(code: String): CappedNodeCheckpoint?
    fun save(item: CappedNodeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNodeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNodeCheckpointStore(
    var items: MutableMap<String, CappedNodeCheckpoint>,
    var order: MutableList<String>,
) : CappedNodeCheckpointStore {
    override fun find(code: String): CappedNodeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNodeCheckpoint) {
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

    override fun list(): MutableList<CappedNodeCheckpoint> {
        val out = mutableListOf<CappedNodeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNodeCheckpointStore(): CappedNodeCheckpointStore {
    return MemoryCappedNodeCheckpointStore(
        mutableMapOf<String, CappedNodeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNodeCheckpointStore(
    var inner: CappedNodeCheckpointStore,
    var cache: TtlCache<CappedNodeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CappedNodeCheckpointStore {
    override fun find(code: String): CappedNodeCheckpoint? {
        val hit = (cache.get(code) as CappedNodeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNodeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNodeCheckpoint> = inner.list()
}

fun newCachedCappedNodeCheckpointStore(inner: CappedNodeCheckpointStore, ttl: Int, clock: Clock): CappedNodeCheckpointStore {
    return CachedCappedNodeCheckpointStore(
        inner = inner,
        cache = newTtlCache<CappedNodeCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped node checkpoints. Empty fields match all. */
data class CappedNodeCheckpointQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNodeCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedNodeCheckpoint>): MutableList<CappedNodeCheckpoint> {
        val matched = filter(items) { candidate: CappedNodeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNodeCheckpoints(store: CappedNodeCheckpointStore, query: CappedNodeCheckpointQuery): MutableList<CappedNodeCheckpoint> {
    return query.apply(store.list())
}
