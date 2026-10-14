package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss scores by code. */
interface NearMissScoreStore {
    fun find(code: String): NearMissScore?
    fun save(item: NearMissScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissScoreStore(
    var items: MutableMap<String, NearMissScore>,
    var order: MutableList<String>,
) : NearMissScoreStore {
    override fun find(code: String): NearMissScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissScore) {
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

    override fun list(): MutableList<NearMissScore> {
        val out = mutableListOf<NearMissScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissScoreStore(): NearMissScoreStore {
    return MemoryNearMissScoreStore(mutableMapOf<String, NearMissScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissScoreStore(
    var inner: NearMissScoreStore,
    var cache: TtlCache<NearMissScore>,
    var hits: Int,
    var misses: Int,
) : NearMissScoreStore {
    override fun find(code: String): NearMissScore? {
        val hit = (cache.get(code) as NearMissScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissScore> = inner.list()
}

fun newCachedNearMissScoreStore(inner: NearMissScoreStore, ttl: Int, clock: Clock): NearMissScoreStore {
    return CachedNearMissScoreStore(
        inner = inner,
        cache = newTtlCache<NearMissScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss scores. Empty fields match all. */
data class NearMissScoreQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<NearMissScore>): MutableList<NearMissScore> {
        val matched = filter(items) { candidate: NearMissScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissScores(store: NearMissScoreStore, query: NearMissScoreQuery): MutableList<NearMissScore> {
    return query.apply(store.list())
}
