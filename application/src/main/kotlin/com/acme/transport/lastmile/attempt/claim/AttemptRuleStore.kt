package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt rules by code. */
interface AttemptRuleStore {
    fun find(code: String): AttemptRule?
    fun save(item: AttemptRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptRuleStore(
    var items: MutableMap<String, AttemptRule>,
    var order: MutableList<String>,
) : AttemptRuleStore {
    override fun find(code: String): AttemptRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptRule) {
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

    override fun list(): MutableList<AttemptRule> {
        val out = mutableListOf<AttemptRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptRuleStore(): AttemptRuleStore {
    return MemoryAttemptRuleStore(mutableMapOf<String, AttemptRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptRuleStore(
    var inner: AttemptRuleStore,
    var cache: TtlCache<AttemptRule>,
    var hits: Int,
    var misses: Int,
) : AttemptRuleStore {
    override fun find(code: String): AttemptRule? {
        val hit = (cache.get(code) as AttemptRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptRule> = inner.list()
}

fun newCachedAttemptRuleStore(inner: AttemptRuleStore, ttl: Int, clock: Clock): AttemptRuleStore {
    return CachedAttemptRuleStore(
        inner = inner,
        cache = newTtlCache<AttemptRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt rules. Empty fields match all. */
data class AttemptRuleQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptRule>): MutableList<AttemptRule> {
        val matched = filter(items) { candidate: AttemptRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptRules(store: AttemptRuleStore, query: AttemptRuleQuery): MutableList<AttemptRule> {
    return query.apply(store.list())
}
