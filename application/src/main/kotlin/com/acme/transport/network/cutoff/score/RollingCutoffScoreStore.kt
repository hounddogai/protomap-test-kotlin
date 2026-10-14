package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff scores by code. */
interface RollingCutoffScoreStore {
    fun find(code: String): RollingCutoffScore?
    fun save(item: RollingCutoffScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffScoreStore(
    var items: MutableMap<String, RollingCutoffScore>,
    var order: MutableList<String>,
) : RollingCutoffScoreStore {
    override fun find(code: String): RollingCutoffScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffScore) {
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

    override fun list(): MutableList<RollingCutoffScore> {
        val out = mutableListOf<RollingCutoffScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffScoreStore(): RollingCutoffScoreStore {
    return MemoryRollingCutoffScoreStore(mutableMapOf<String, RollingCutoffScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffScoreStore(
    var inner: RollingCutoffScoreStore,
    var cache: TtlCache<RollingCutoffScore>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffScoreStore {
    override fun find(code: String): RollingCutoffScore? {
        val hit = (cache.get(code) as RollingCutoffScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffScore> = inner.list()
}

fun newCachedRollingCutoffScoreStore(inner: RollingCutoffScoreStore, ttl: Int, clock: Clock): RollingCutoffScoreStore {
    return CachedRollingCutoffScoreStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff scores. Empty fields match all. */
data class RollingCutoffScoreQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCutoffScore>): MutableList<RollingCutoffScore> {
        val matched = filter(items) { candidate: RollingCutoffScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffScores(store: RollingCutoffScoreStore, query: RollingCutoffScoreQuery): MutableList<RollingCutoffScore> {
    return query.apply(store.list())
}
