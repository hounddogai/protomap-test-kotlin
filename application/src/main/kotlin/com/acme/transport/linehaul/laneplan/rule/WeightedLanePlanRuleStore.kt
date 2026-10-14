package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan rules by code. */
interface WeightedLanePlanRuleStore {
    fun find(code: String): WeightedLanePlanRule?
    fun save(item: WeightedLanePlanRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanRuleStore(
    var items: MutableMap<String, WeightedLanePlanRule>,
    var order: MutableList<String>,
) : WeightedLanePlanRuleStore {
    override fun find(code: String): WeightedLanePlanRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanRule) {
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

    override fun list(): MutableList<WeightedLanePlanRule> {
        val out = mutableListOf<WeightedLanePlanRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanRuleStore(): WeightedLanePlanRuleStore {
    return MemoryWeightedLanePlanRuleStore(
        mutableMapOf<String, WeightedLanePlanRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanRuleStore(
    var inner: WeightedLanePlanRuleStore,
    var cache: TtlCache<WeightedLanePlanRule>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanRuleStore {
    override fun find(code: String): WeightedLanePlanRule? {
        val hit = (cache.get(code) as WeightedLanePlanRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanRule> = inner.list()
}

fun newCachedWeightedLanePlanRuleStore(inner: WeightedLanePlanRuleStore, ttl: Int, clock: Clock): WeightedLanePlanRuleStore {
    return CachedWeightedLanePlanRuleStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan rules. Empty fields match all. */
data class WeightedLanePlanRuleQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLanePlanRule>): MutableList<WeightedLanePlanRule> {
        val matched = filter(items) { candidate: WeightedLanePlanRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanRules(store: WeightedLanePlanRuleStore, query: WeightedLanePlanRuleQuery): MutableList<WeightedLanePlanRule> {
    return query.apply(store.list())
}
