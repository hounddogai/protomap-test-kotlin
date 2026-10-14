package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal locker rules by code. */
interface SeasonalLockerRuleStore {
    fun find(code: String): SeasonalLockerRule?
    fun save(item: SeasonalLockerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalLockerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalLockerRuleStore(
    var items: MutableMap<String, SeasonalLockerRule>,
    var order: MutableList<String>,
) : SeasonalLockerRuleStore {
    override fun find(code: String): SeasonalLockerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalLockerRule) {
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

    override fun list(): MutableList<SeasonalLockerRule> {
        val out = mutableListOf<SeasonalLockerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalLockerRuleStore(): SeasonalLockerRuleStore {
    return MemorySeasonalLockerRuleStore(mutableMapOf<String, SeasonalLockerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalLockerRuleStore(
    var inner: SeasonalLockerRuleStore,
    var cache: TtlCache<SeasonalLockerRule>,
    var hits: Int,
    var misses: Int,
) : SeasonalLockerRuleStore {
    override fun find(code: String): SeasonalLockerRule? {
        val hit = (cache.get(code) as SeasonalLockerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalLockerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalLockerRule> = inner.list()
}

fun newCachedSeasonalLockerRuleStore(inner: SeasonalLockerRuleStore, ttl: Int, clock: Clock): SeasonalLockerRuleStore {
    return CachedSeasonalLockerRuleStore(
        inner = inner,
        cache = newTtlCache<SeasonalLockerRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal locker rules. Empty fields match all. */
data class SeasonalLockerRuleQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalLockerRule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalLockerRule>): MutableList<SeasonalLockerRule> {
        val matched = filter(items) { candidate: SeasonalLockerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalLockerRules(store: SeasonalLockerRuleStore, query: SeasonalLockerRuleQuery): MutableList<SeasonalLockerRule> {
    return query.apply(store.list())
}
