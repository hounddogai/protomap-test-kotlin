package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity scores by code. */
interface CapacityScoreStore {
    fun find(code: String): CapacityScore?
    fun save(item: CapacityScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityScoreStore(
    var items: MutableMap<String, CapacityScore>,
    var order: MutableList<String>,
) : CapacityScoreStore {
    override fun find(code: String): CapacityScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityScore) {
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

    override fun list(): MutableList<CapacityScore> {
        val out = mutableListOf<CapacityScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityScoreStore(): CapacityScoreStore {
    return MemoryCapacityScoreStore(mutableMapOf<String, CapacityScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityScoreStore(
    var inner: CapacityScoreStore,
    var cache: TtlCache<CapacityScore>,
    var hits: Int,
    var misses: Int,
) : CapacityScoreStore {
    override fun find(code: String): CapacityScore? {
        val hit = (cache.get(code) as CapacityScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityScore> = inner.list()
}

fun newCachedCapacityScoreStore(inner: CapacityScoreStore, ttl: Int, clock: Clock): CapacityScoreStore {
    return CachedCapacityScoreStore(
        inner = inner,
        cache = newTtlCache<CapacityScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity scores. Empty fields match all. */
data class CapacityScoreQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityScore>): MutableList<CapacityScore> {
        val matched = filter(items) { candidate: CapacityScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityScores(store: CapacityScoreStore, query: CapacityScoreQuery): MutableList<CapacityScore> {
    return query.apply(store.list())
}
