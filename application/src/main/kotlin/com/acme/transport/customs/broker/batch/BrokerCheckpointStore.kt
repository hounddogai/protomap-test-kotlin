package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker checkpoints by code. */
interface BrokerCheckpointStore {
    fun find(code: String): BrokerCheckpoint?
    fun save(item: BrokerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerCheckpointStore(
    var items: MutableMap<String, BrokerCheckpoint>,
    var order: MutableList<String>,
) : BrokerCheckpointStore {
    override fun find(code: String): BrokerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerCheckpoint) {
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

    override fun list(): MutableList<BrokerCheckpoint> {
        val out = mutableListOf<BrokerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerCheckpointStore(): BrokerCheckpointStore {
    return MemoryBrokerCheckpointStore(mutableMapOf<String, BrokerCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerCheckpointStore(
    var inner: BrokerCheckpointStore,
    var cache: TtlCache<BrokerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : BrokerCheckpointStore {
    override fun find(code: String): BrokerCheckpoint? {
        val hit = (cache.get(code) as BrokerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerCheckpoint> = inner.list()
}

fun newCachedBrokerCheckpointStore(inner: BrokerCheckpointStore, ttl: Int, clock: Clock): BrokerCheckpointStore {
    return CachedBrokerCheckpointStore(
        inner = inner,
        cache = newTtlCache<BrokerCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker checkpoints. Empty fields match all. */
data class BrokerCheckpointQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerCheckpoint>): MutableList<BrokerCheckpoint> {
        val matched = filter(items) { candidate: BrokerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerCheckpoints(store: BrokerCheckpointStore, query: BrokerCheckpointQuery): MutableList<BrokerCheckpoint> {
    return query.apply(store.list())
}
