package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict scorecard rules by code. */
interface StrictScorecardRuleStore {
    fun find(code: String): StrictScorecardRule?
    fun save(item: StrictScorecardRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictScorecardRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictScorecardRuleStore(
    var items: MutableMap<String, StrictScorecardRule>,
    var order: MutableList<String>,
) : StrictScorecardRuleStore {
    override fun find(code: String): StrictScorecardRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictScorecardRule) {
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

    override fun list(): MutableList<StrictScorecardRule> {
        val out = mutableListOf<StrictScorecardRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictScorecardRuleStore(): StrictScorecardRuleStore {
    return MemoryStrictScorecardRuleStore(mutableMapOf<String, StrictScorecardRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictScorecardRuleStore(
    var inner: StrictScorecardRuleStore,
    var cache: TtlCache<StrictScorecardRule>,
    var hits: Int,
    var misses: Int,
) : StrictScorecardRuleStore {
    override fun find(code: String): StrictScorecardRule? {
        val hit = (cache.get(code) as StrictScorecardRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictScorecardRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictScorecardRule> = inner.list()
}

fun newCachedStrictScorecardRuleStore(inner: StrictScorecardRuleStore, ttl: Int, clock: Clock): StrictScorecardRuleStore {
    return CachedStrictScorecardRuleStore(
        inner = inner,
        cache = newTtlCache<StrictScorecardRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict scorecard rules. Empty fields match all. */
data class StrictScorecardRuleQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictScorecardRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictScorecardRule>): MutableList<StrictScorecardRule> {
        val matched = filter(items) { candidate: StrictScorecardRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictScorecardRules(store: StrictScorecardRuleStore, query: StrictScorecardRuleQuery): MutableList<StrictScorecardRule> {
    return query.apply(store.list())
}
