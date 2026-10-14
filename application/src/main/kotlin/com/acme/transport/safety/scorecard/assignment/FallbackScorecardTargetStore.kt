package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback scorecard targets by code. */
interface FallbackScorecardTargetStore {
    fun find(code: String): FallbackScorecardTarget?
    fun save(item: FallbackScorecardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackScorecardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackScorecardTargetStore(
    var items: MutableMap<String, FallbackScorecardTarget>,
    var order: MutableList<String>,
) : FallbackScorecardTargetStore {
    override fun find(code: String): FallbackScorecardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackScorecardTarget) {
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

    override fun list(): MutableList<FallbackScorecardTarget> {
        val out = mutableListOf<FallbackScorecardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackScorecardTargetStore(): FallbackScorecardTargetStore {
    return MemoryFallbackScorecardTargetStore(
        mutableMapOf<String, FallbackScorecardTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackScorecardTargetStore(
    var inner: FallbackScorecardTargetStore,
    var cache: TtlCache<FallbackScorecardTarget>,
    var hits: Int,
    var misses: Int,
) : FallbackScorecardTargetStore {
    override fun find(code: String): FallbackScorecardTarget? {
        val hit = (cache.get(code) as FallbackScorecardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackScorecardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackScorecardTarget> = inner.list()
}

fun newCachedFallbackScorecardTargetStore(inner: FallbackScorecardTargetStore, ttl: Int, clock: Clock): FallbackScorecardTargetStore {
    return CachedFallbackScorecardTargetStore(
        inner = inner,
        cache = newTtlCache<FallbackScorecardTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback scorecard targets. Empty fields match all. */
data class FallbackScorecardTargetQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackScorecardTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<FallbackScorecardTarget>): MutableList<FallbackScorecardTarget> {
        val matched = filter(items) { candidate: FallbackScorecardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackScorecardTargets(store: FallbackScorecardTargetStore, query: FallbackScorecardTargetQuery): MutableList<FallbackScorecardTarget> {
    return query.apply(store.list())
}
