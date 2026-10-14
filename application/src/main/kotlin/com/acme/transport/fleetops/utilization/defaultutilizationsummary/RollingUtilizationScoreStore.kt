package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling utilization scores by code. */
interface RollingUtilizationScoreStore {
    fun find(code: String): RollingUtilizationScore?
    fun save(item: RollingUtilizationScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingUtilizationScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingUtilizationScoreStore(
    var items: MutableMap<String, RollingUtilizationScore>,
    var order: MutableList<String>,
) : RollingUtilizationScoreStore {
    override fun find(code: String): RollingUtilizationScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingUtilizationScore) {
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

    override fun list(): MutableList<RollingUtilizationScore> {
        val out = mutableListOf<RollingUtilizationScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingUtilizationScoreStore(): RollingUtilizationScoreStore {
    return MemoryRollingUtilizationScoreStore(
        mutableMapOf<String, RollingUtilizationScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingUtilizationScoreStore(
    var inner: RollingUtilizationScoreStore,
    var cache: TtlCache<RollingUtilizationScore>,
    var hits: Int,
    var misses: Int,
) : RollingUtilizationScoreStore {
    override fun find(code: String): RollingUtilizationScore? {
        val hit = (cache.get(code) as RollingUtilizationScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingUtilizationScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingUtilizationScore> = inner.list()
}

fun newCachedRollingUtilizationScoreStore(inner: RollingUtilizationScoreStore, ttl: Int, clock: Clock): RollingUtilizationScoreStore {
    return CachedRollingUtilizationScoreStore(
        inner = inner,
        cache = newTtlCache<RollingUtilizationScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling utilization scores. Empty fields match all. */
data class RollingUtilizationScoreQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingUtilizationScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingUtilizationScore>): MutableList<RollingUtilizationScore> {
        val matched = filter(items) { candidate: RollingUtilizationScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingUtilizationScores(store: RollingUtilizationScoreStore, query: RollingUtilizationScoreQuery): MutableList<RollingUtilizationScore> {
    return query.apply(store.list())
}
