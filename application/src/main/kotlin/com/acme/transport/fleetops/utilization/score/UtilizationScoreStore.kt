package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization scores by code. */
interface UtilizationScoreStore {
    fun find(code: String): UtilizationScore?
    fun save(item: UtilizationScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationScoreStore(
    var items: MutableMap<String, UtilizationScore>,
    var order: MutableList<String>,
) : UtilizationScoreStore {
    override fun find(code: String): UtilizationScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationScore) {
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

    override fun list(): MutableList<UtilizationScore> {
        val out = mutableListOf<UtilizationScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationScoreStore(): UtilizationScoreStore {
    return MemoryUtilizationScoreStore(mutableMapOf<String, UtilizationScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationScoreStore(
    var inner: UtilizationScoreStore,
    var cache: TtlCache<UtilizationScore>,
    var hits: Int,
    var misses: Int,
) : UtilizationScoreStore {
    override fun find(code: String): UtilizationScore? {
        val hit = (cache.get(code) as UtilizationScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationScore> = inner.list()
}

fun newCachedUtilizationScoreStore(inner: UtilizationScoreStore, ttl: Int, clock: Clock): UtilizationScoreStore {
    return CachedUtilizationScoreStore(
        inner = inner,
        cache = newTtlCache<UtilizationScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization scores. Empty fields match all. */
data class UtilizationScoreQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationScore>): MutableList<UtilizationScore> {
        val matched = filter(items) { candidate: UtilizationScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationScores(store: UtilizationScoreStore, query: UtilizationScoreQuery): MutableList<UtilizationScore> {
    return query.apply(store.list())
}
