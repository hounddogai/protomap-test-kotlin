package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary roster rules by code. */
interface PrimaryRosterRuleStore {
    fun find(code: String): PrimaryRosterRule?
    fun save(item: PrimaryRosterRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRosterRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRosterRuleStore(
    var items: MutableMap<String, PrimaryRosterRule>,
    var order: MutableList<String>,
) : PrimaryRosterRuleStore {
    override fun find(code: String): PrimaryRosterRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRosterRule) {
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

    override fun list(): MutableList<PrimaryRosterRule> {
        val out = mutableListOf<PrimaryRosterRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRosterRuleStore(): PrimaryRosterRuleStore {
    return MemoryPrimaryRosterRuleStore(mutableMapOf<String, PrimaryRosterRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRosterRuleStore(
    var inner: PrimaryRosterRuleStore,
    var cache: TtlCache<PrimaryRosterRule>,
    var hits: Int,
    var misses: Int,
) : PrimaryRosterRuleStore {
    override fun find(code: String): PrimaryRosterRule? {
        val hit = (cache.get(code) as PrimaryRosterRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRosterRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRosterRule> = inner.list()
}

fun newCachedPrimaryRosterRuleStore(inner: PrimaryRosterRuleStore, ttl: Int, clock: Clock): PrimaryRosterRuleStore {
    return CachedPrimaryRosterRuleStore(
        inner = inner,
        cache = newTtlCache<PrimaryRosterRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary roster rules. Empty fields match all. */
data class PrimaryRosterRuleQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRosterRule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRosterRule>): MutableList<PrimaryRosterRule> {
        val matched = filter(items) { candidate: PrimaryRosterRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRosterRules(store: PrimaryRosterRuleStore, query: PrimaryRosterRuleQuery): MutableList<PrimaryRosterRule> {
    return query.apply(store.list())
}
