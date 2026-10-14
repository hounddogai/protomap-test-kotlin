package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict cutoff rules by code. */
interface StrictCutoffRuleStore {
    fun find(code: String): StrictCutoffRule?
    fun save(item: StrictCutoffRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCutoffRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCutoffRuleStore(
    var items: MutableMap<String, StrictCutoffRule>,
    var order: MutableList<String>,
) : StrictCutoffRuleStore {
    override fun find(code: String): StrictCutoffRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCutoffRule) {
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

    override fun list(): MutableList<StrictCutoffRule> {
        val out = mutableListOf<StrictCutoffRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCutoffRuleStore(): StrictCutoffRuleStore {
    return MemoryStrictCutoffRuleStore(mutableMapOf<String, StrictCutoffRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCutoffRuleStore(
    var inner: StrictCutoffRuleStore,
    var cache: TtlCache<StrictCutoffRule>,
    var hits: Int,
    var misses: Int,
) : StrictCutoffRuleStore {
    override fun find(code: String): StrictCutoffRule? {
        val hit = (cache.get(code) as StrictCutoffRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCutoffRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCutoffRule> = inner.list()
}

fun newCachedStrictCutoffRuleStore(inner: StrictCutoffRuleStore, ttl: Int, clock: Clock): StrictCutoffRuleStore {
    return CachedStrictCutoffRuleStore(
        inner = inner,
        cache = newTtlCache<StrictCutoffRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict cutoff rules. Empty fields match all. */
data class StrictCutoffRuleQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCutoffRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCutoffRule>): MutableList<StrictCutoffRule> {
        val matched = filter(items) { candidate: StrictCutoffRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCutoffRules(store: StrictCutoffRuleStore, query: StrictCutoffRuleQuery): MutableList<StrictCutoffRule> {
    return query.apply(store.list())
}
