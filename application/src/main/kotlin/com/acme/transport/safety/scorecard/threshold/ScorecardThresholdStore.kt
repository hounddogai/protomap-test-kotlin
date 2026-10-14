package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard thresholds by code. */
interface ScorecardThresholdStore {
    fun find(code: String): ScorecardThreshold?
    fun save(item: ScorecardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardThresholdStore(
    var items: MutableMap<String, ScorecardThreshold>,
    var order: MutableList<String>,
) : ScorecardThresholdStore {
    override fun find(code: String): ScorecardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardThreshold) {
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

    override fun list(): MutableList<ScorecardThreshold> {
        val out = mutableListOf<ScorecardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardThresholdStore(): ScorecardThresholdStore {
    return MemoryScorecardThresholdStore(mutableMapOf<String, ScorecardThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardThresholdStore(
    var inner: ScorecardThresholdStore,
    var cache: TtlCache<ScorecardThreshold>,
    var hits: Int,
    var misses: Int,
) : ScorecardThresholdStore {
    override fun find(code: String): ScorecardThreshold? {
        val hit = (cache.get(code) as ScorecardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardThreshold> = inner.list()
}

fun newCachedScorecardThresholdStore(inner: ScorecardThresholdStore, ttl: Int, clock: Clock): ScorecardThresholdStore {
    return CachedScorecardThresholdStore(
        inner = inner,
        cache = newTtlCache<ScorecardThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard thresholds. Empty fields match all. */
data class ScorecardThresholdQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardThreshold>): MutableList<ScorecardThreshold> {
        val matched = filter(items) { candidate: ScorecardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardThresholds(store: ScorecardThresholdStore, query: ScorecardThresholdQuery): MutableList<ScorecardThreshold> {
    return query.apply(store.list())
}
