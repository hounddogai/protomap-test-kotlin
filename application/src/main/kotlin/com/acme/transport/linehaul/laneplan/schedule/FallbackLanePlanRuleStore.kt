package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan rules by code. */
interface FallbackLanePlanRuleStore {
    fun find(code: String): FallbackLanePlanRule?
    fun save(item: FallbackLanePlanRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanRuleStore(
    var items: MutableMap<String, FallbackLanePlanRule>,
    var order: MutableList<String>,
) : FallbackLanePlanRuleStore {
    override fun find(code: String): FallbackLanePlanRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanRule) {
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

    override fun list(): MutableList<FallbackLanePlanRule> {
        val out = mutableListOf<FallbackLanePlanRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanRuleStore(): FallbackLanePlanRuleStore {
    return MemoryFallbackLanePlanRuleStore(
        mutableMapOf<String, FallbackLanePlanRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanRuleStore(
    var inner: FallbackLanePlanRuleStore,
    var cache: TtlCache<FallbackLanePlanRule>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanRuleStore {
    override fun find(code: String): FallbackLanePlanRule? {
        val hit = (cache.get(code) as FallbackLanePlanRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanRule> = inner.list()
}

fun newCachedFallbackLanePlanRuleStore(inner: FallbackLanePlanRuleStore, ttl: Int, clock: Clock): FallbackLanePlanRuleStore {
    return CachedFallbackLanePlanRuleStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan rules. Empty fields match all. */
data class FallbackLanePlanRuleQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanRule>): MutableList<FallbackLanePlanRule> {
        val matched = filter(items) { candidate: FallbackLanePlanRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanRules(store: FallbackLanePlanRuleStore, query: FallbackLanePlanRuleQuery): MutableList<FallbackLanePlanRule> {
    return query.apply(store.list())
}
