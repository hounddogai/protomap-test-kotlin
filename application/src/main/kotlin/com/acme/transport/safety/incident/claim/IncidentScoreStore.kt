package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident scores by code. */
interface IncidentScoreStore {
    fun find(code: String): IncidentScore?
    fun save(item: IncidentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentScoreStore(
    var items: MutableMap<String, IncidentScore>,
    var order: MutableList<String>,
) : IncidentScoreStore {
    override fun find(code: String): IncidentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentScore) {
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

    override fun list(): MutableList<IncidentScore> {
        val out = mutableListOf<IncidentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentScoreStore(): IncidentScoreStore {
    return MemoryIncidentScoreStore(mutableMapOf<String, IncidentScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentScoreStore(
    var inner: IncidentScoreStore,
    var cache: TtlCache<IncidentScore>,
    var hits: Int,
    var misses: Int,
) : IncidentScoreStore {
    override fun find(code: String): IncidentScore? {
        val hit = (cache.get(code) as IncidentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentScore> = inner.list()
}

fun newCachedIncidentScoreStore(inner: IncidentScoreStore, ttl: Int, clock: Clock): IncidentScoreStore {
    return CachedIncidentScoreStore(
        inner = inner,
        cache = newTtlCache<IncidentScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident scores. Empty fields match all. */
data class IncidentScoreQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentScore): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentScore>): MutableList<IncidentScore> {
        val matched = filter(items) { candidate: IncidentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentScores(store: IncidentScoreStore, query: IncidentScoreQuery): MutableList<IncidentScore> {
    return query.apply(store.list())
}
