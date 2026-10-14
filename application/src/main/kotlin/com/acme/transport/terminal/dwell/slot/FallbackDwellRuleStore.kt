package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell rules by code. */
interface FallbackDwellRuleStore {
    fun find(code: String): FallbackDwellRule?
    fun save(item: FallbackDwellRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellRuleStore(
    var items: MutableMap<String, FallbackDwellRule>,
    var order: MutableList<String>,
) : FallbackDwellRuleStore {
    override fun find(code: String): FallbackDwellRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellRule) {
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

    override fun list(): MutableList<FallbackDwellRule> {
        val out = mutableListOf<FallbackDwellRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellRuleStore(): FallbackDwellRuleStore {
    return MemoryFallbackDwellRuleStore(mutableMapOf<String, FallbackDwellRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellRuleStore(
    var inner: FallbackDwellRuleStore,
    var cache: TtlCache<FallbackDwellRule>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellRuleStore {
    override fun find(code: String): FallbackDwellRule? {
        val hit = (cache.get(code) as FallbackDwellRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellRule> = inner.list()
}

fun newCachedFallbackDwellRuleStore(inner: FallbackDwellRuleStore, ttl: Int, clock: Clock): FallbackDwellRuleStore {
    return CachedFallbackDwellRuleStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell rules. Empty fields match all. */
data class FallbackDwellRuleQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellRule>): MutableList<FallbackDwellRule> {
        val matched = filter(items) { candidate: FallbackDwellRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellRules(store: FallbackDwellRuleStore, query: FallbackDwellRuleQuery): MutableList<FallbackDwellRule> {
    return query.apply(store.list())
}
