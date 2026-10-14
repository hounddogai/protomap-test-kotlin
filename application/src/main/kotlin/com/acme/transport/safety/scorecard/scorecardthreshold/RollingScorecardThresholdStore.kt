package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling scorecard thresholds by code. */
interface RollingScorecardThresholdStore {
    fun find(code: String): RollingScorecardThreshold?
    fun save(item: RollingScorecardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingScorecardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingScorecardThresholdStore(
    var items: MutableMap<String, RollingScorecardThreshold>,
    var order: MutableList<String>,
) : RollingScorecardThresholdStore {
    override fun find(code: String): RollingScorecardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingScorecardThreshold) {
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

    override fun list(): MutableList<RollingScorecardThreshold> {
        val out = mutableListOf<RollingScorecardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingScorecardThresholdStore(): RollingScorecardThresholdStore {
    return MemoryRollingScorecardThresholdStore(
        mutableMapOf<String, RollingScorecardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingScorecardThresholdStore(
    var inner: RollingScorecardThresholdStore,
    var cache: TtlCache<RollingScorecardThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingScorecardThresholdStore {
    override fun find(code: String): RollingScorecardThreshold? {
        val hit = (cache.get(code) as RollingScorecardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingScorecardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingScorecardThreshold> = inner.list()
}

fun newCachedRollingScorecardThresholdStore(inner: RollingScorecardThresholdStore, ttl: Int, clock: Clock): RollingScorecardThresholdStore {
    return CachedRollingScorecardThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingScorecardThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling scorecard thresholds. Empty fields match all. */
data class RollingScorecardThresholdQuery(
    var lane: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingScorecardThreshold): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RollingScorecardThreshold>): MutableList<RollingScorecardThreshold> {
        val matched = filter(items) { candidate: RollingScorecardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingScorecardThresholds(store: RollingScorecardThresholdStore, query: RollingScorecardThresholdQuery): MutableList<RollingScorecardThreshold> {
    return query.apply(store.list())
}
