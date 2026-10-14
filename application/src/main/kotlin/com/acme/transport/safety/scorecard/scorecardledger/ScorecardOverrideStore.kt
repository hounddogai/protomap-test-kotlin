package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard overrides by code. */
interface ScorecardOverrideStore {
    fun find(code: String): ScorecardOverride?
    fun save(item: ScorecardOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardOverrideStore(
    var items: MutableMap<String, ScorecardOverride>,
    var order: MutableList<String>,
) : ScorecardOverrideStore {
    override fun find(code: String): ScorecardOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardOverride) {
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

    override fun list(): MutableList<ScorecardOverride> {
        val out = mutableListOf<ScorecardOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardOverrideStore(): ScorecardOverrideStore {
    return MemoryScorecardOverrideStore(mutableMapOf<String, ScorecardOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardOverrideStore(
    var inner: ScorecardOverrideStore,
    var cache: TtlCache<ScorecardOverride>,
    var hits: Int,
    var misses: Int,
) : ScorecardOverrideStore {
    override fun find(code: String): ScorecardOverride? {
        val hit = (cache.get(code) as ScorecardOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardOverride> = inner.list()
}

fun newCachedScorecardOverrideStore(inner: ScorecardOverrideStore, ttl: Int, clock: Clock): ScorecardOverrideStore {
    return CachedScorecardOverrideStore(
        inner = inner,
        cache = newTtlCache<ScorecardOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard overrides. Empty fields match all. */
data class ScorecardOverrideQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardOverride>): MutableList<ScorecardOverride> {
        val matched = filter(items) { candidate: ScorecardOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardOverrides(store: ScorecardOverrideStore, query: ScorecardOverrideQuery): MutableList<ScorecardOverride> {
    return query.apply(store.list())
}
