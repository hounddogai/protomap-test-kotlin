package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal emission factor checkpoints by code. */
interface SeasonalEmissionFactorCheckpointStore {
    fun find(code: String): SeasonalEmissionFactorCheckpoint?
    fun save(item: SeasonalEmissionFactorCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEmissionFactorCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEmissionFactorCheckpointStore(
    var items: MutableMap<String, SeasonalEmissionFactorCheckpoint>,
    var order: MutableList<String>,
) : SeasonalEmissionFactorCheckpointStore {
    override fun find(code: String): SeasonalEmissionFactorCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEmissionFactorCheckpoint) {
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

    override fun list(): MutableList<SeasonalEmissionFactorCheckpoint> {
        val out = mutableListOf<SeasonalEmissionFactorCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEmissionFactorCheckpointStore(): SeasonalEmissionFactorCheckpointStore {
    return MemorySeasonalEmissionFactorCheckpointStore(
        mutableMapOf<String, SeasonalEmissionFactorCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEmissionFactorCheckpointStore(
    var inner: SeasonalEmissionFactorCheckpointStore,
    var cache: TtlCache<SeasonalEmissionFactorCheckpoint>,
    var hits: Int,
    var misses: Int,
) : SeasonalEmissionFactorCheckpointStore {
    override fun find(code: String): SeasonalEmissionFactorCheckpoint? {
        val hit = (cache.get(code) as SeasonalEmissionFactorCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEmissionFactorCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEmissionFactorCheckpoint> = inner.list()
}

fun newCachedSeasonalEmissionFactorCheckpointStore(inner: SeasonalEmissionFactorCheckpointStore, ttl: Int, clock: Clock): SeasonalEmissionFactorCheckpointStore {
    return CachedSeasonalEmissionFactorCheckpointStore(
        inner = inner,
        cache = newTtlCache<SeasonalEmissionFactorCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal emission factor checkpoints. Empty fields match all. */
data class SeasonalEmissionFactorCheckpointQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEmissionFactorCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<SeasonalEmissionFactorCheckpoint>): MutableList<SeasonalEmissionFactorCheckpoint> {
        val matched = filter(items) { candidate: SeasonalEmissionFactorCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEmissionFactorCheckpoints(store: SeasonalEmissionFactorCheckpointStore, query: SeasonalEmissionFactorCheckpointQuery): MutableList<SeasonalEmissionFactorCheckpoint> {
    return query.apply(store.list())
}
