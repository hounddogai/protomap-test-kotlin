package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive scorecard checkpoints by code. */
interface AdaptiveScorecardCheckpointStore {
    fun find(code: String): AdaptiveScorecardCheckpoint?
    fun save(item: AdaptiveScorecardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveScorecardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveScorecardCheckpointStore(
    var items: MutableMap<String, AdaptiveScorecardCheckpoint>,
    var order: MutableList<String>,
) : AdaptiveScorecardCheckpointStore {
    override fun find(code: String): AdaptiveScorecardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveScorecardCheckpoint) {
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

    override fun list(): MutableList<AdaptiveScorecardCheckpoint> {
        val out = mutableListOf<AdaptiveScorecardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveScorecardCheckpointStore(): AdaptiveScorecardCheckpointStore {
    return MemoryAdaptiveScorecardCheckpointStore(
        mutableMapOf<String, AdaptiveScorecardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveScorecardCheckpointStore(
    var inner: AdaptiveScorecardCheckpointStore,
    var cache: TtlCache<AdaptiveScorecardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AdaptiveScorecardCheckpointStore {
    override fun find(code: String): AdaptiveScorecardCheckpoint? {
        val hit = (cache.get(code) as AdaptiveScorecardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveScorecardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveScorecardCheckpoint> = inner.list()
}

fun newCachedAdaptiveScorecardCheckpointStore(inner: AdaptiveScorecardCheckpointStore, ttl: Int, clock: Clock): AdaptiveScorecardCheckpointStore {
    return CachedAdaptiveScorecardCheckpointStore(
        inner = inner,
        cache = newTtlCache<AdaptiveScorecardCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive scorecard checkpoints. Empty fields match all. */
data class AdaptiveScorecardCheckpointQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveScorecardCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveScorecardCheckpoint>): MutableList<AdaptiveScorecardCheckpoint> {
        val matched = filter(items) { candidate: AdaptiveScorecardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveScorecardCheckpoints(store: AdaptiveScorecardCheckpointStore, query: AdaptiveScorecardCheckpointQuery): MutableList<AdaptiveScorecardCheckpoint> {
    return query.apply(store.list())
}
