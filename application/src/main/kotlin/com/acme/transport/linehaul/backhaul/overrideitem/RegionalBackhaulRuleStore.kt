package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional backhaul rules by code. */
interface RegionalBackhaulRuleStore {
    fun find(code: String): RegionalBackhaulRule?
    fun save(item: RegionalBackhaulRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBackhaulRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBackhaulRuleStore(
    var items: MutableMap<String, RegionalBackhaulRule>,
    var order: MutableList<String>,
) : RegionalBackhaulRuleStore {
    override fun find(code: String): RegionalBackhaulRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBackhaulRule) {
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

    override fun list(): MutableList<RegionalBackhaulRule> {
        val out = mutableListOf<RegionalBackhaulRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBackhaulRuleStore(): RegionalBackhaulRuleStore {
    return MemoryRegionalBackhaulRuleStore(
        mutableMapOf<String, RegionalBackhaulRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBackhaulRuleStore(
    var inner: RegionalBackhaulRuleStore,
    var cache: TtlCache<RegionalBackhaulRule>,
    var hits: Int,
    var misses: Int,
) : RegionalBackhaulRuleStore {
    override fun find(code: String): RegionalBackhaulRule? {
        val hit = (cache.get(code) as RegionalBackhaulRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBackhaulRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBackhaulRule> = inner.list()
}

fun newCachedRegionalBackhaulRuleStore(inner: RegionalBackhaulRuleStore, ttl: Int, clock: Clock): RegionalBackhaulRuleStore {
    return CachedRegionalBackhaulRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalBackhaulRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional backhaul rules. Empty fields match all. */
data class RegionalBackhaulRuleQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBackhaulRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RegionalBackhaulRule>): MutableList<RegionalBackhaulRule> {
        val matched = filter(items) { candidate: RegionalBackhaulRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBackhaulRules(store: RegionalBackhaulRuleStore, query: RegionalBackhaulRuleQuery): MutableList<RegionalBackhaulRule> {
    return query.apply(store.list())
}
