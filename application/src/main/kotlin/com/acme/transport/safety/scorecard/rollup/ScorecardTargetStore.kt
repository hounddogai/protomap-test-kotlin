package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard targets by code. */
interface ScorecardTargetStore {
    fun find(code: String): ScorecardTarget?
    fun save(item: ScorecardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardTargetStore(
    var items: MutableMap<String, ScorecardTarget>,
    var order: MutableList<String>,
) : ScorecardTargetStore {
    override fun find(code: String): ScorecardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardTarget) {
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

    override fun list(): MutableList<ScorecardTarget> {
        val out = mutableListOf<ScorecardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardTargetStore(): ScorecardTargetStore {
    return MemoryScorecardTargetStore(mutableMapOf<String, ScorecardTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardTargetStore(
    var inner: ScorecardTargetStore,
    var cache: TtlCache<ScorecardTarget>,
    var hits: Int,
    var misses: Int,
) : ScorecardTargetStore {
    override fun find(code: String): ScorecardTarget? {
        val hit = (cache.get(code) as ScorecardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardTarget> = inner.list()
}

fun newCachedScorecardTargetStore(inner: ScorecardTargetStore, ttl: Int, clock: Clock): ScorecardTargetStore {
    return CachedScorecardTargetStore(
        inner = inner,
        cache = newTtlCache<ScorecardTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard targets. Empty fields match all. */
data class ScorecardTargetQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScorecardTarget>): MutableList<ScorecardTarget> {
        val matched = filter(items) { candidate: ScorecardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardTargets(store: ScorecardTargetStore, query: ScorecardTargetQuery): MutableList<ScorecardTarget> {
    return query.apply(store.list())
}
