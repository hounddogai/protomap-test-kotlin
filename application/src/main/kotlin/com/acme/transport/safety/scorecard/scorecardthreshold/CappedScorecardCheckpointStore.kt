package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped scorecard checkpoints by code. */
interface CappedScorecardCheckpointStore {
    fun find(code: String): CappedScorecardCheckpoint?
    fun save(item: CappedScorecardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedScorecardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedScorecardCheckpointStore(
    var items: MutableMap<String, CappedScorecardCheckpoint>,
    var order: MutableList<String>,
) : CappedScorecardCheckpointStore {
    override fun find(code: String): CappedScorecardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedScorecardCheckpoint) {
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

    override fun list(): MutableList<CappedScorecardCheckpoint> {
        val out = mutableListOf<CappedScorecardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedScorecardCheckpointStore(): CappedScorecardCheckpointStore {
    return MemoryCappedScorecardCheckpointStore(
        mutableMapOf<String, CappedScorecardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedScorecardCheckpointStore(
    var inner: CappedScorecardCheckpointStore,
    var cache: TtlCache<CappedScorecardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CappedScorecardCheckpointStore {
    override fun find(code: String): CappedScorecardCheckpoint? {
        val hit = (cache.get(code) as CappedScorecardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedScorecardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedScorecardCheckpoint> = inner.list()
}

fun newCachedCappedScorecardCheckpointStore(inner: CappedScorecardCheckpointStore, ttl: Int, clock: Clock): CappedScorecardCheckpointStore {
    return CachedCappedScorecardCheckpointStore(
        inner = inner,
        cache = newTtlCache<CappedScorecardCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped scorecard checkpoints. Empty fields match all. */
data class CappedScorecardCheckpointQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedScorecardCheckpoint): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedScorecardCheckpoint>): MutableList<CappedScorecardCheckpoint> {
        val matched = filter(items) { candidate: CappedScorecardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedScorecardCheckpoints(store: CappedScorecardCheckpointStore, query: CappedScorecardCheckpointQuery): MutableList<CappedScorecardCheckpoint> {
    return query.apply(store.list())
}
