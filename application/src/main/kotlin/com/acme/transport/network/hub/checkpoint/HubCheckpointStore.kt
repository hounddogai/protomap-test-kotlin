package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub checkpoints by code. */
interface HubCheckpointStore {
    fun find(code: String): HubCheckpoint?
    fun save(item: HubCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubCheckpointStore(
    var items: MutableMap<String, HubCheckpoint>,
    var order: MutableList<String>,
) : HubCheckpointStore {
    override fun find(code: String): HubCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubCheckpoint) {
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

    override fun list(): MutableList<HubCheckpoint> {
        val out = mutableListOf<HubCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubCheckpointStore(): HubCheckpointStore {
    return MemoryHubCheckpointStore(mutableMapOf<String, HubCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubCheckpointStore(
    var inner: HubCheckpointStore,
    var cache: TtlCache<HubCheckpoint>,
    var hits: Int,
    var misses: Int,
) : HubCheckpointStore {
    override fun find(code: String): HubCheckpoint? {
        val hit = (cache.get(code) as HubCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubCheckpoint> = inner.list()
}

fun newCachedHubCheckpointStore(inner: HubCheckpointStore, ttl: Int, clock: Clock): HubCheckpointStore {
    return CachedHubCheckpointStore(
        inner = inner,
        cache = newTtlCache<HubCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub checkpoints. Empty fields match all. */
data class HubCheckpointQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubCheckpoint>): MutableList<HubCheckpoint> {
        val matched = filter(items) { candidate: HubCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubCheckpoints(store: HubCheckpointStore, query: HubCheckpointQuery): MutableList<HubCheckpoint> {
    return query.apply(store.list())
}
