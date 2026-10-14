package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive accessorial checkpoints by code. */
interface AdaptiveAccessorialCheckpointStore {
    fun find(code: String): AdaptiveAccessorialCheckpoint?
    fun save(item: AdaptiveAccessorialCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAccessorialCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAccessorialCheckpointStore(
    var items: MutableMap<String, AdaptiveAccessorialCheckpoint>,
    var order: MutableList<String>,
) : AdaptiveAccessorialCheckpointStore {
    override fun find(code: String): AdaptiveAccessorialCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAccessorialCheckpoint) {
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

    override fun list(): MutableList<AdaptiveAccessorialCheckpoint> {
        val out = mutableListOf<AdaptiveAccessorialCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAccessorialCheckpointStore(): AdaptiveAccessorialCheckpointStore {
    return MemoryAdaptiveAccessorialCheckpointStore(
        mutableMapOf<String, AdaptiveAccessorialCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAccessorialCheckpointStore(
    var inner: AdaptiveAccessorialCheckpointStore,
    var cache: TtlCache<AdaptiveAccessorialCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAccessorialCheckpointStore {
    override fun find(code: String): AdaptiveAccessorialCheckpoint? {
        val hit = (cache.get(code) as AdaptiveAccessorialCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAccessorialCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAccessorialCheckpoint> = inner.list()
}

fun newCachedAdaptiveAccessorialCheckpointStore(inner: AdaptiveAccessorialCheckpointStore, ttl: Int, clock: Clock): AdaptiveAccessorialCheckpointStore {
    return CachedAdaptiveAccessorialCheckpointStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAccessorialCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive accessorial checkpoints. Empty fields match all. */
data class AdaptiveAccessorialCheckpointQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAccessorialCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveAccessorialCheckpoint>): MutableList<AdaptiveAccessorialCheckpoint> {
        val matched = filter(items) { candidate: AdaptiveAccessorialCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAccessorialCheckpoints(store: AdaptiveAccessorialCheckpointStore, query: AdaptiveAccessorialCheckpointQuery): MutableList<AdaptiveAccessorialCheckpoint> {
    return query.apply(store.list())
}
