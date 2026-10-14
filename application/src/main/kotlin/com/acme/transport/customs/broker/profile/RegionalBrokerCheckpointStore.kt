package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker checkpoints by code. */
interface RegionalBrokerCheckpointStore {
    fun find(code: String): RegionalBrokerCheckpoint?
    fun save(item: RegionalBrokerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerCheckpointStore(
    var items: MutableMap<String, RegionalBrokerCheckpoint>,
    var order: MutableList<String>,
) : RegionalBrokerCheckpointStore {
    override fun find(code: String): RegionalBrokerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerCheckpoint) {
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

    override fun list(): MutableList<RegionalBrokerCheckpoint> {
        val out = mutableListOf<RegionalBrokerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerCheckpointStore(): RegionalBrokerCheckpointStore {
    return MemoryRegionalBrokerCheckpointStore(
        mutableMapOf<String, RegionalBrokerCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerCheckpointStore(
    var inner: RegionalBrokerCheckpointStore,
    var cache: TtlCache<RegionalBrokerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerCheckpointStore {
    override fun find(code: String): RegionalBrokerCheckpoint? {
        val hit = (cache.get(code) as RegionalBrokerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerCheckpoint> = inner.list()
}

fun newCachedRegionalBrokerCheckpointStore(inner: RegionalBrokerCheckpointStore, ttl: Int, clock: Clock): RegionalBrokerCheckpointStore {
    return CachedRegionalBrokerCheckpointStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker checkpoints. Empty fields match all. */
data class RegionalBrokerCheckpointQuery(
    var region: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerCheckpoint): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalBrokerCheckpoint>): MutableList<RegionalBrokerCheckpoint> {
        val matched = filter(items) { candidate: RegionalBrokerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerCheckpoints(store: RegionalBrokerCheckpointStore, query: RegionalBrokerCheckpointQuery): MutableList<RegionalBrokerCheckpoint> {
    return query.apply(store.list())
}
