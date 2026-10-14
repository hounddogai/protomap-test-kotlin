package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan scores by code. */
interface LanePlanScoreStore {
    fun find(code: String): LanePlanScore?
    fun save(item: LanePlanScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanScoreStore(
    var items: MutableMap<String, LanePlanScore>,
    var order: MutableList<String>,
) : LanePlanScoreStore {
    override fun find(code: String): LanePlanScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanScore) {
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

    override fun list(): MutableList<LanePlanScore> {
        val out = mutableListOf<LanePlanScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanScoreStore(): LanePlanScoreStore {
    return MemoryLanePlanScoreStore(mutableMapOf<String, LanePlanScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanScoreStore(
    var inner: LanePlanScoreStore,
    var cache: TtlCache<LanePlanScore>,
    var hits: Int,
    var misses: Int,
) : LanePlanScoreStore {
    override fun find(code: String): LanePlanScore? {
        val hit = (cache.get(code) as LanePlanScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanScore> = inner.list()
}

fun newCachedLanePlanScoreStore(inner: LanePlanScoreStore, ttl: Int, clock: Clock): LanePlanScoreStore {
    return CachedLanePlanScoreStore(
        inner = inner,
        cache = newTtlCache<LanePlanScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan scores. Empty fields match all. */
data class LanePlanScoreQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanScore>): MutableList<LanePlanScore> {
        val matched = filter(items) { candidate: LanePlanScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanScores(store: LanePlanScoreStore, query: LanePlanScoreQuery): MutableList<LanePlanScore> {
    return query.apply(store.list())
}
