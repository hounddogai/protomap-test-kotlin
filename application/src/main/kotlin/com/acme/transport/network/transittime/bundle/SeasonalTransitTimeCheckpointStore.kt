package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal transit time checkpoints by code. */
interface SeasonalTransitTimeCheckpointStore {
    fun find(code: String): SeasonalTransitTimeCheckpoint?
    fun save(item: SeasonalTransitTimeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTransitTimeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTransitTimeCheckpointStore(
    var items: MutableMap<String, SeasonalTransitTimeCheckpoint>,
    var order: MutableList<String>,
) : SeasonalTransitTimeCheckpointStore {
    override fun find(code: String): SeasonalTransitTimeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTransitTimeCheckpoint) {
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

    override fun list(): MutableList<SeasonalTransitTimeCheckpoint> {
        val out = mutableListOf<SeasonalTransitTimeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTransitTimeCheckpointStore(): SeasonalTransitTimeCheckpointStore {
    return MemorySeasonalTransitTimeCheckpointStore(
        mutableMapOf<String, SeasonalTransitTimeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTransitTimeCheckpointStore(
    var inner: SeasonalTransitTimeCheckpointStore,
    var cache: TtlCache<SeasonalTransitTimeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : SeasonalTransitTimeCheckpointStore {
    override fun find(code: String): SeasonalTransitTimeCheckpoint? {
        val hit = (cache.get(code) as SeasonalTransitTimeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTransitTimeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTransitTimeCheckpoint> = inner.list()
}

fun newCachedSeasonalTransitTimeCheckpointStore(inner: SeasonalTransitTimeCheckpointStore, ttl: Int, clock: Clock): SeasonalTransitTimeCheckpointStore {
    return CachedSeasonalTransitTimeCheckpointStore(
        inner = inner,
        cache = newTtlCache<SeasonalTransitTimeCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal transit time checkpoints. Empty fields match all. */
data class SeasonalTransitTimeCheckpointQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTransitTimeCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableList<SeasonalTransitTimeCheckpoint> {
        val matched = filter(items) { candidate: SeasonalTransitTimeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTransitTimeCheckpoints(store: SeasonalTransitTimeCheckpointStore, query: SeasonalTransitTimeCheckpointQuery): MutableList<SeasonalTransitTimeCheckpoint> {
    return query.apply(store.list())
}
