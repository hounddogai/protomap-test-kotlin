package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling incident scores by code. */
interface RollingIncidentScoreStore {
    fun find(code: String): RollingIncidentScore?
    fun save(item: RollingIncidentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIncidentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIncidentScoreStore(
    var items: MutableMap<String, RollingIncidentScore>,
    var order: MutableList<String>,
) : RollingIncidentScoreStore {
    override fun find(code: String): RollingIncidentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIncidentScore) {
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

    override fun list(): MutableList<RollingIncidentScore> {
        val out = mutableListOf<RollingIncidentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIncidentScoreStore(): RollingIncidentScoreStore {
    return MemoryRollingIncidentScoreStore(
        mutableMapOf<String, RollingIncidentScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIncidentScoreStore(
    var inner: RollingIncidentScoreStore,
    var cache: TtlCache<RollingIncidentScore>,
    var hits: Int,
    var misses: Int,
) : RollingIncidentScoreStore {
    override fun find(code: String): RollingIncidentScore? {
        val hit = (cache.get(code) as RollingIncidentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIncidentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIncidentScore> = inner.list()
}

fun newCachedRollingIncidentScoreStore(inner: RollingIncidentScoreStore, ttl: Int, clock: Clock): RollingIncidentScoreStore {
    return CachedRollingIncidentScoreStore(
        inner = inner,
        cache = newTtlCache<RollingIncidentScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling incident scores. Empty fields match all. */
data class RollingIncidentScoreQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIncidentScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIncidentScore>): MutableList<RollingIncidentScore> {
        val matched = filter(items) { candidate: RollingIncidentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIncidentScores(store: RollingIncidentScoreStore, query: RollingIncidentScoreQuery): MutableList<RollingIncidentScore> {
    return query.apply(store.list())
}
