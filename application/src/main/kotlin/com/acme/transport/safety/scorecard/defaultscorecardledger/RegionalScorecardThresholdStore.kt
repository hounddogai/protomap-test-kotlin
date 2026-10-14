package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional scorecard thresholds by code. */
interface RegionalScorecardThresholdStore {
    fun find(code: String): RegionalScorecardThreshold?
    fun save(item: RegionalScorecardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalScorecardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalScorecardThresholdStore(
    var items: MutableMap<String, RegionalScorecardThreshold>,
    var order: MutableList<String>,
) : RegionalScorecardThresholdStore {
    override fun find(code: String): RegionalScorecardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalScorecardThreshold) {
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

    override fun list(): MutableList<RegionalScorecardThreshold> {
        val out = mutableListOf<RegionalScorecardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalScorecardThresholdStore(): RegionalScorecardThresholdStore {
    return MemoryRegionalScorecardThresholdStore(
        mutableMapOf<String, RegionalScorecardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalScorecardThresholdStore(
    var inner: RegionalScorecardThresholdStore,
    var cache: TtlCache<RegionalScorecardThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalScorecardThresholdStore {
    override fun find(code: String): RegionalScorecardThreshold? {
        val hit = (cache.get(code) as RegionalScorecardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalScorecardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalScorecardThreshold> = inner.list()
}

fun newCachedRegionalScorecardThresholdStore(inner: RegionalScorecardThresholdStore, ttl: Int, clock: Clock): RegionalScorecardThresholdStore {
    return CachedRegionalScorecardThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalScorecardThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional scorecard thresholds. Empty fields match all. */
data class RegionalScorecardThresholdQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalScorecardThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalScorecardThreshold>): MutableList<RegionalScorecardThreshold> {
        val matched = filter(items) { candidate: RegionalScorecardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalScorecardThresholds(store: RegionalScorecardThresholdStore, query: RegionalScorecardThresholdQuery): MutableList<RegionalScorecardThreshold> {
    return query.apply(store.list())
}
