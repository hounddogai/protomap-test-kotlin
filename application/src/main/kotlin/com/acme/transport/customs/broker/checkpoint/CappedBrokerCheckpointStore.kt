package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped broker checkpoints by code. */
interface CappedBrokerCheckpointStore {
    fun find(code: String): CappedBrokerCheckpoint?
    fun save(item: CappedBrokerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBrokerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBrokerCheckpointStore(
    var items: MutableMap<String, CappedBrokerCheckpoint>,
    var order: MutableList<String>,
) : CappedBrokerCheckpointStore {
    override fun find(code: String): CappedBrokerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBrokerCheckpoint) {
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

    override fun list(): MutableList<CappedBrokerCheckpoint> {
        val out = mutableListOf<CappedBrokerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBrokerCheckpointStore(): CappedBrokerCheckpointStore {
    return MemoryCappedBrokerCheckpointStore(
        mutableMapOf<String, CappedBrokerCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBrokerCheckpointStore(
    var inner: CappedBrokerCheckpointStore,
    var cache: TtlCache<CappedBrokerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CappedBrokerCheckpointStore {
    override fun find(code: String): CappedBrokerCheckpoint? {
        val hit = (cache.get(code) as CappedBrokerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBrokerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBrokerCheckpoint> = inner.list()
}

fun newCachedCappedBrokerCheckpointStore(inner: CappedBrokerCheckpointStore, ttl: Int, clock: Clock): CappedBrokerCheckpointStore {
    return CachedCappedBrokerCheckpointStore(
        inner = inner,
        cache = newTtlCache<CappedBrokerCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped broker checkpoints. Empty fields match all. */
data class CappedBrokerCheckpointQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBrokerCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedBrokerCheckpoint>): MutableList<CappedBrokerCheckpoint> {
        val matched = filter(items) { candidate: CappedBrokerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBrokerCheckpoints(store: CappedBrokerCheckpointStore, query: CappedBrokerCheckpointQuery): MutableList<CappedBrokerCheckpoint> {
    return query.apply(store.list())
}
