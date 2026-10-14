package com.acme.transport.crewing.qualification.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling qualification rules by code. */
interface RollingQualificationRuleStore {
    fun find(code: String): RollingQualificationRule?
    fun save(item: RollingQualificationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingQualificationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingQualificationRuleStore(
    var items: MutableMap<String, RollingQualificationRule>,
    var order: MutableList<String>,
) : RollingQualificationRuleStore {
    override fun find(code: String): RollingQualificationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingQualificationRule) {
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

    override fun list(): MutableList<RollingQualificationRule> {
        val out = mutableListOf<RollingQualificationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingQualificationRuleStore(): RollingQualificationRuleStore {
    return MemoryRollingQualificationRuleStore(
        mutableMapOf<String, RollingQualificationRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingQualificationRuleStore(
    var inner: RollingQualificationRuleStore,
    var cache: TtlCache<RollingQualificationRule>,
    var hits: Int,
    var misses: Int,
) : RollingQualificationRuleStore {
    override fun find(code: String): RollingQualificationRule? {
        val hit = (cache.get(code) as RollingQualificationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingQualificationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingQualificationRule> = inner.list()
}

fun newCachedRollingQualificationRuleStore(inner: RollingQualificationRuleStore, ttl: Int, clock: Clock): RollingQualificationRuleStore {
    return CachedRollingQualificationRuleStore(
        inner = inner,
        cache = newTtlCache<RollingQualificationRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling qualification rules. Empty fields match all. */
data class RollingQualificationRuleQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingQualificationRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RollingQualificationRule>): MutableList<RollingQualificationRule> {
        val matched = filter(items) { candidate: RollingQualificationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingQualificationRules(store: RollingQualificationRuleStore, query: RollingQualificationRuleQuery): MutableList<RollingQualificationRule> {
    return query.apply(store.list())
}
