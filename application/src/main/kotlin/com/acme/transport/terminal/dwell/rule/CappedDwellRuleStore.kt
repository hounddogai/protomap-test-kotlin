package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell rules by code. */
interface CappedDwellRuleStore {
    fun find(code: String): CappedDwellRule?
    fun save(item: CappedDwellRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellRuleStore(
    var items: MutableMap<String, CappedDwellRule>,
    var order: MutableList<String>,
) : CappedDwellRuleStore {
    override fun find(code: String): CappedDwellRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellRule) {
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

    override fun list(): MutableList<CappedDwellRule> {
        val out = mutableListOf<CappedDwellRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellRuleStore(): CappedDwellRuleStore {
    return MemoryCappedDwellRuleStore(mutableMapOf<String, CappedDwellRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellRuleStore(
    var inner: CappedDwellRuleStore,
    var cache: TtlCache<CappedDwellRule>,
    var hits: Int,
    var misses: Int,
) : CappedDwellRuleStore {
    override fun find(code: String): CappedDwellRule? {
        val hit = (cache.get(code) as CappedDwellRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellRule> = inner.list()
}

fun newCachedCappedDwellRuleStore(inner: CappedDwellRuleStore, ttl: Int, clock: Clock): CappedDwellRuleStore {
    return CachedCappedDwellRuleStore(
        inner = inner,
        cache = newTtlCache<CappedDwellRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell rules. Empty fields match all. */
data class CappedDwellRuleQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellRule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDwellRule>): MutableList<CappedDwellRule> {
        val matched = filter(items) { candidate: CappedDwellRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellRules(store: CappedDwellRuleStore, query: CappedDwellRuleQuery): MutableList<CappedDwellRule> {
    return query.apply(store.list())
}
