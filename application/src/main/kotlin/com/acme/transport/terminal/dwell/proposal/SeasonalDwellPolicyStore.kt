package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dwell policies by code. */
interface SeasonalDwellPolicyStore {
    fun find(code: String): SeasonalDwellPolicy?
    fun save(item: SeasonalDwellPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDwellPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDwellPolicyStore(
    var items: MutableMap<String, SeasonalDwellPolicy>,
    var order: MutableList<String>,
) : SeasonalDwellPolicyStore {
    override fun find(code: String): SeasonalDwellPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDwellPolicy) {
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

    override fun list(): MutableList<SeasonalDwellPolicy> {
        val out = mutableListOf<SeasonalDwellPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDwellPolicyStore(): SeasonalDwellPolicyStore {
    return MemorySeasonalDwellPolicyStore(mutableMapOf<String, SeasonalDwellPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDwellPolicyStore(
    var inner: SeasonalDwellPolicyStore,
    var cache: TtlCache<SeasonalDwellPolicy>,
    var hits: Int,
    var misses: Int,
) : SeasonalDwellPolicyStore {
    override fun find(code: String): SeasonalDwellPolicy? {
        val hit = (cache.get(code) as SeasonalDwellPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDwellPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDwellPolicy> = inner.list()
}

fun newCachedSeasonalDwellPolicyStore(inner: SeasonalDwellPolicyStore, ttl: Int, clock: Clock): SeasonalDwellPolicyStore {
    return CachedSeasonalDwellPolicyStore(
        inner = inner,
        cache = newTtlCache<SeasonalDwellPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dwell policies. Empty fields match all. */
data class SeasonalDwellPolicyQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDwellPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDwellPolicy>): MutableList<SeasonalDwellPolicy> {
        val matched = filter(items) { candidate: SeasonalDwellPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDwellPolicies(store: SeasonalDwellPolicyStore, query: SeasonalDwellPolicyQuery): MutableList<SeasonalDwellPolicy> {
    return query.apply(store.list())
}
