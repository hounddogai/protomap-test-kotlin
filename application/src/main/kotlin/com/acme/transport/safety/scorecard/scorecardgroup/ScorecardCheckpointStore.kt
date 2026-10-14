package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard checkpoints by code. */
interface ScorecardCheckpointStore {
    fun find(code: String): ScorecardCheckpoint?
    fun save(item: ScorecardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardCheckpointStore(
    var items: MutableMap<String, ScorecardCheckpoint>,
    var order: MutableList<String>,
) : ScorecardCheckpointStore {
    override fun find(code: String): ScorecardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardCheckpoint) {
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

    override fun list(): MutableList<ScorecardCheckpoint> {
        val out = mutableListOf<ScorecardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardCheckpointStore(): ScorecardCheckpointStore {
    return MemoryScorecardCheckpointStore(mutableMapOf<String, ScorecardCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardCheckpointStore(
    var inner: ScorecardCheckpointStore,
    var cache: TtlCache<ScorecardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ScorecardCheckpointStore {
    override fun find(code: String): ScorecardCheckpoint? {
        val hit = (cache.get(code) as ScorecardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardCheckpoint> = inner.list()
}

fun newCachedScorecardCheckpointStore(inner: ScorecardCheckpointStore, ttl: Int, clock: Clock): ScorecardCheckpointStore {
    return CachedScorecardCheckpointStore(
        inner = inner,
        cache = newTtlCache<ScorecardCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard checkpoints. Empty fields match all. */
data class ScorecardCheckpointQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardCheckpoint>): MutableList<ScorecardCheckpoint> {
        val matched = filter(items) { candidate: ScorecardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardCheckpoints(store: ScorecardCheckpointStore, query: ScorecardCheckpointQuery): MutableList<ScorecardCheckpoint> {
    return query.apply(store.list())
}
