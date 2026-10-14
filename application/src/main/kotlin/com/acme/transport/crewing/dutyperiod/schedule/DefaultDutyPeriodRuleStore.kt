package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default duty period rules by code. */
interface DefaultDutyPeriodRuleStore {
    fun find(code: String): DefaultDutyPeriodRule?
    fun save(item: DefaultDutyPeriodRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDutyPeriodRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDutyPeriodRuleStore(
    var items: MutableMap<String, DefaultDutyPeriodRule>,
    var order: MutableList<String>,
) : DefaultDutyPeriodRuleStore {
    override fun find(code: String): DefaultDutyPeriodRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDutyPeriodRule) {
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

    override fun list(): MutableList<DefaultDutyPeriodRule> {
        val out = mutableListOf<DefaultDutyPeriodRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDutyPeriodRuleStore(): DefaultDutyPeriodRuleStore {
    return MemoryDefaultDutyPeriodRuleStore(
        mutableMapOf<String, DefaultDutyPeriodRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDutyPeriodRuleStore(
    var inner: DefaultDutyPeriodRuleStore,
    var cache: TtlCache<DefaultDutyPeriodRule>,
    var hits: Int,
    var misses: Int,
) : DefaultDutyPeriodRuleStore {
    override fun find(code: String): DefaultDutyPeriodRule? {
        val hit = (cache.get(code) as DefaultDutyPeriodRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDutyPeriodRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDutyPeriodRule> = inner.list()
}

fun newCachedDefaultDutyPeriodRuleStore(inner: DefaultDutyPeriodRuleStore, ttl: Int, clock: Clock): DefaultDutyPeriodRuleStore {
    return CachedDefaultDutyPeriodRuleStore(
        inner = inner,
        cache = newTtlCache<DefaultDutyPeriodRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default duty period rules. Empty fields match all. */
data class DefaultDutyPeriodRuleQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDutyPeriodRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDutyPeriodRule>): MutableList<DefaultDutyPeriodRule> {
        val matched = filter(items) { candidate: DefaultDutyPeriodRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDutyPeriodRules(store: DefaultDutyPeriodRuleStore, query: DefaultDutyPeriodRuleQuery): MutableList<DefaultDutyPeriodRule> {
    return query.apply(store.list())
}
