package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period rules by code. */
interface DutyPeriodRuleStore {
    fun find(code: String): DutyPeriodRule?
    fun save(item: DutyPeriodRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodRuleStore(
    var items: MutableMap<String, DutyPeriodRule>,
    var order: MutableList<String>,
) : DutyPeriodRuleStore {
    override fun find(code: String): DutyPeriodRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodRule) {
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

    override fun list(): MutableList<DutyPeriodRule> {
        val out = mutableListOf<DutyPeriodRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodRuleStore(): DutyPeriodRuleStore {
    return MemoryDutyPeriodRuleStore(mutableMapOf<String, DutyPeriodRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodRuleStore(
    var inner: DutyPeriodRuleStore,
    var cache: TtlCache<DutyPeriodRule>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodRuleStore {
    override fun find(code: String): DutyPeriodRule? {
        val hit = (cache.get(code) as DutyPeriodRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodRule> = inner.list()
}

fun newCachedDutyPeriodRuleStore(inner: DutyPeriodRuleStore, ttl: Int, clock: Clock): DutyPeriodRuleStore {
    return CachedDutyPeriodRuleStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period rules. Empty fields match all. */
data class DutyPeriodRuleQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodRule>): MutableList<DutyPeriodRule> {
        val matched = filter(items) { candidate: DutyPeriodRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodRules(store: DutyPeriodRuleStore, query: DutyPeriodRuleQuery): MutableList<DutyPeriodRule> {
    return query.apply(store.list())
}
