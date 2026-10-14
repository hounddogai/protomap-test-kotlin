package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break rules by code. */
interface RestBreakRuleStore {
    fun find(code: String): RestBreakRule?
    fun save(item: RestBreakRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakRuleStore(
    var items: MutableMap<String, RestBreakRule>,
    var order: MutableList<String>,
) : RestBreakRuleStore {
    override fun find(code: String): RestBreakRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakRule) {
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

    override fun list(): MutableList<RestBreakRule> {
        val out = mutableListOf<RestBreakRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakRuleStore(): RestBreakRuleStore {
    return MemoryRestBreakRuleStore(mutableMapOf<String, RestBreakRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakRuleStore(
    var inner: RestBreakRuleStore,
    var cache: TtlCache<RestBreakRule>,
    var hits: Int,
    var misses: Int,
) : RestBreakRuleStore {
    override fun find(code: String): RestBreakRule? {
        val hit = (cache.get(code) as RestBreakRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakRule> = inner.list()
}

fun newCachedRestBreakRuleStore(inner: RestBreakRuleStore, ttl: Int, clock: Clock): RestBreakRuleStore {
    return CachedRestBreakRuleStore(
        inner = inner,
        cache = newTtlCache<RestBreakRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break rules. Empty fields match all. */
data class RestBreakRuleQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakRule>): MutableList<RestBreakRule> {
        val matched = filter(items) { candidate: RestBreakRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakRules(store: RestBreakRuleStore, query: RestBreakRuleQuery): MutableList<RestBreakRule> {
    return query.apply(store.list())
}
