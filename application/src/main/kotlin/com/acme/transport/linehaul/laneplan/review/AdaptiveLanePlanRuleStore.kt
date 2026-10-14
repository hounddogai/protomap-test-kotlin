package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan rules by code. */
interface AdaptiveLanePlanRuleStore {
    fun find(code: String): AdaptiveLanePlanRule?
    fun save(item: AdaptiveLanePlanRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanRuleStore(
    var items: MutableMap<String, AdaptiveLanePlanRule>,
    var order: MutableList<String>,
) : AdaptiveLanePlanRuleStore {
    override fun find(code: String): AdaptiveLanePlanRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanRule) {
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

    override fun list(): MutableList<AdaptiveLanePlanRule> {
        val out = mutableListOf<AdaptiveLanePlanRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanRuleStore(): AdaptiveLanePlanRuleStore {
    return MemoryAdaptiveLanePlanRuleStore(
        mutableMapOf<String, AdaptiveLanePlanRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanRuleStore(
    var inner: AdaptiveLanePlanRuleStore,
    var cache: TtlCache<AdaptiveLanePlanRule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanRuleStore {
    override fun find(code: String): AdaptiveLanePlanRule? {
        val hit = (cache.get(code) as AdaptiveLanePlanRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanRule> = inner.list()
}

fun newCachedAdaptiveLanePlanRuleStore(inner: AdaptiveLanePlanRuleStore, ttl: Int, clock: Clock): AdaptiveLanePlanRuleStore {
    return CachedAdaptiveLanePlanRuleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan rules. Empty fields match all. */
data class AdaptiveLanePlanRuleQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanRule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLanePlanRule>): MutableList<AdaptiveLanePlanRule> {
        val matched = filter(items) { candidate: AdaptiveLanePlanRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanRules(store: AdaptiveLanePlanRuleStore, query: AdaptiveLanePlanRuleQuery): MutableList<AdaptiveLanePlanRule> {
    return query.apply(store.list())
}
