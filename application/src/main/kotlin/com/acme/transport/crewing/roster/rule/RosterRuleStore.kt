package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster rules by code. */
interface RosterRuleStore {
    fun find(code: String): RosterRule?
    fun save(item: RosterRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterRuleStore(
    var items: MutableMap<String, RosterRule>,
    var order: MutableList<String>,
) : RosterRuleStore {
    override fun find(code: String): RosterRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterRule) {
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

    override fun list(): MutableList<RosterRule> {
        val out = mutableListOf<RosterRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterRuleStore(): RosterRuleStore {
    return MemoryRosterRuleStore(mutableMapOf<String, RosterRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterRuleStore(
    var inner: RosterRuleStore,
    var cache: TtlCache<RosterRule>,
    var hits: Int,
    var misses: Int,
) : RosterRuleStore {
    override fun find(code: String): RosterRule? {
        val hit = (cache.get(code) as RosterRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterRule> = inner.list()
}

fun newCachedRosterRuleStore(inner: RosterRuleStore, ttl: Int, clock: Clock): RosterRuleStore {
    return CachedRosterRuleStore(
        inner = inner,
        cache = newTtlCache<RosterRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster rules. Empty fields match all. */
data class RosterRuleQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterRule>): MutableList<RosterRule> {
        val matched = filter(items) { candidate: RosterRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterRules(store: RosterRuleStore, query: RosterRuleQuery): MutableList<RosterRule> {
    return query.apply(store.list())
}
