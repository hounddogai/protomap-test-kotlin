package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling rules by code. */
interface IdlingRuleStore {
    fun find(code: String): IdlingRule?
    fun save(item: IdlingRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingRuleStore(
    var items: MutableMap<String, IdlingRule>,
    var order: MutableList<String>,
) : IdlingRuleStore {
    override fun find(code: String): IdlingRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingRule) {
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

    override fun list(): MutableList<IdlingRule> {
        val out = mutableListOf<IdlingRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingRuleStore(): IdlingRuleStore {
    return MemoryIdlingRuleStore(mutableMapOf<String, IdlingRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingRuleStore(
    var inner: IdlingRuleStore,
    var cache: TtlCache<IdlingRule>,
    var hits: Int,
    var misses: Int,
) : IdlingRuleStore {
    override fun find(code: String): IdlingRule? {
        val hit = (cache.get(code) as IdlingRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingRule> = inner.list()
}

fun newCachedIdlingRuleStore(inner: IdlingRuleStore, ttl: Int, clock: Clock): IdlingRuleStore {
    return CachedIdlingRuleStore(
        inner = inner,
        cache = newTtlCache<IdlingRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling rules. Empty fields match all. */
data class IdlingRuleQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingRule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingRule>): MutableList<IdlingRule> {
        val matched = filter(items) { candidate: IdlingRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingRules(store: IdlingRuleStore, query: IdlingRuleQuery): MutableList<IdlingRule> {
    return query.apply(store.list())
}
