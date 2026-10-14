package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional rest break rules by code. */
interface RegionalRestBreakRuleStore {
    fun find(code: String): RegionalRestBreakRule?
    fun save(item: RegionalRestBreakRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRestBreakRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRestBreakRuleStore(
    var items: MutableMap<String, RegionalRestBreakRule>,
    var order: MutableList<String>,
) : RegionalRestBreakRuleStore {
    override fun find(code: String): RegionalRestBreakRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRestBreakRule) {
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

    override fun list(): MutableList<RegionalRestBreakRule> {
        val out = mutableListOf<RegionalRestBreakRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRestBreakRuleStore(): RegionalRestBreakRuleStore {
    return MemoryRegionalRestBreakRuleStore(
        mutableMapOf<String, RegionalRestBreakRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRestBreakRuleStore(
    var inner: RegionalRestBreakRuleStore,
    var cache: TtlCache<RegionalRestBreakRule>,
    var hits: Int,
    var misses: Int,
) : RegionalRestBreakRuleStore {
    override fun find(code: String): RegionalRestBreakRule? {
        val hit = (cache.get(code) as RegionalRestBreakRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRestBreakRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRestBreakRule> = inner.list()
}

fun newCachedRegionalRestBreakRuleStore(inner: RegionalRestBreakRuleStore, ttl: Int, clock: Clock): RegionalRestBreakRuleStore {
    return CachedRegionalRestBreakRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalRestBreakRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional rest break rules. Empty fields match all. */
data class RegionalRestBreakRuleQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRestBreakRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RegionalRestBreakRule>): MutableList<RegionalRestBreakRule> {
        val matched = filter(items) { candidate: RegionalRestBreakRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRestBreakRules(store: RegionalRestBreakRuleStore, query: RegionalRestBreakRuleQuery): MutableList<RegionalRestBreakRule> {
    return query.apply(store.list())
}
