package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default roster rules by code. */
interface DefaultRosterRuleStore {
    fun find(code: String): DefaultRosterRule?
    fun save(item: DefaultRosterRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRosterRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRosterRuleStore(
    var items: MutableMap<String, DefaultRosterRule>,
    var order: MutableList<String>,
) : DefaultRosterRuleStore {
    override fun find(code: String): DefaultRosterRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRosterRule) {
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

    override fun list(): MutableList<DefaultRosterRule> {
        val out = mutableListOf<DefaultRosterRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRosterRuleStore(): DefaultRosterRuleStore {
    return MemoryDefaultRosterRuleStore(mutableMapOf<String, DefaultRosterRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRosterRuleStore(
    var inner: DefaultRosterRuleStore,
    var cache: TtlCache<DefaultRosterRule>,
    var hits: Int,
    var misses: Int,
) : DefaultRosterRuleStore {
    override fun find(code: String): DefaultRosterRule? {
        val hit = (cache.get(code) as DefaultRosterRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRosterRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRosterRule> = inner.list()
}

fun newCachedDefaultRosterRuleStore(inner: DefaultRosterRuleStore, ttl: Int, clock: Clock): DefaultRosterRuleStore {
    return CachedDefaultRosterRuleStore(
        inner = inner,
        cache = newTtlCache<DefaultRosterRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default roster rules. Empty fields match all. */
data class DefaultRosterRuleQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRosterRule): Boolean {
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

    fun apply(items: MutableList<DefaultRosterRule>): MutableList<DefaultRosterRule> {
        val matched = filter(items) { candidate: DefaultRosterRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRosterRules(store: DefaultRosterRuleStore, query: DefaultRosterRuleQuery): MutableList<DefaultRosterRule> {
    return query.apply(store.list())
}
