package com.acme.transport.crewing.roster.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster rules by code. */
interface StrictRosterRuleStore {
    fun find(code: String): StrictRosterRule?
    fun save(item: StrictRosterRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterRuleStore(
    var items: MutableMap<String, StrictRosterRule>,
    var order: MutableList<String>,
) : StrictRosterRuleStore {
    override fun find(code: String): StrictRosterRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterRule) {
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

    override fun list(): MutableList<StrictRosterRule> {
        val out = mutableListOf<StrictRosterRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterRuleStore(): StrictRosterRuleStore {
    return MemoryStrictRosterRuleStore(mutableMapOf<String, StrictRosterRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterRuleStore(
    var inner: StrictRosterRuleStore,
    var cache: TtlCache<StrictRosterRule>,
    var hits: Int,
    var misses: Int,
) : StrictRosterRuleStore {
    override fun find(code: String): StrictRosterRule? {
        val hit = (cache.get(code) as StrictRosterRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterRule> = inner.list()
}

fun newCachedStrictRosterRuleStore(inner: StrictRosterRuleStore, ttl: Int, clock: Clock): StrictRosterRuleStore {
    return CachedStrictRosterRuleStore(
        inner = inner,
        cache = newTtlCache<StrictRosterRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster rules. Empty fields match all. */
data class StrictRosterRuleQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<StrictRosterRule>): MutableList<StrictRosterRule> {
        val matched = filter(items) { candidate: StrictRosterRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterRules(store: StrictRosterRuleStore, query: StrictRosterRuleQuery): MutableList<StrictRosterRule> {
    return query.apply(store.list())
}
