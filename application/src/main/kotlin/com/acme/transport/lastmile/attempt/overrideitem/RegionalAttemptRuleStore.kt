package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional attempt rules by code. */
interface RegionalAttemptRuleStore {
    fun find(code: String): RegionalAttemptRule?
    fun save(item: RegionalAttemptRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAttemptRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAttemptRuleStore(
    var items: MutableMap<String, RegionalAttemptRule>,
    var order: MutableList<String>,
) : RegionalAttemptRuleStore {
    override fun find(code: String): RegionalAttemptRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAttemptRule) {
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

    override fun list(): MutableList<RegionalAttemptRule> {
        val out = mutableListOf<RegionalAttemptRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAttemptRuleStore(): RegionalAttemptRuleStore {
    return MemoryRegionalAttemptRuleStore(mutableMapOf<String, RegionalAttemptRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAttemptRuleStore(
    var inner: RegionalAttemptRuleStore,
    var cache: TtlCache<RegionalAttemptRule>,
    var hits: Int,
    var misses: Int,
) : RegionalAttemptRuleStore {
    override fun find(code: String): RegionalAttemptRule? {
        val hit = (cache.get(code) as RegionalAttemptRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAttemptRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAttemptRule> = inner.list()
}

fun newCachedRegionalAttemptRuleStore(inner: RegionalAttemptRuleStore, ttl: Int, clock: Clock): RegionalAttemptRuleStore {
    return CachedRegionalAttemptRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalAttemptRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional attempt rules. Empty fields match all. */
data class RegionalAttemptRuleQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAttemptRule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAttemptRule>): MutableList<RegionalAttemptRule> {
        val matched = filter(items) { candidate: RegionalAttemptRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAttemptRules(store: RegionalAttemptRuleStore, query: RegionalAttemptRuleQuery): MutableList<RegionalAttemptRule> {
    return query.apply(store.list())
}
