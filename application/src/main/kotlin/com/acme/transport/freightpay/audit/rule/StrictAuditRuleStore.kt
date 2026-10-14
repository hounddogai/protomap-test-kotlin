package com.acme.transport.freightpay.audit.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict audit rules by code. */
interface StrictAuditRuleStore {
    fun find(code: String): StrictAuditRule?
    fun save(item: StrictAuditRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAuditRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAuditRuleStore(
    var items: MutableMap<String, StrictAuditRule>,
    var order: MutableList<String>,
) : StrictAuditRuleStore {
    override fun find(code: String): StrictAuditRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAuditRule) {
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

    override fun list(): MutableList<StrictAuditRule> {
        val out = mutableListOf<StrictAuditRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAuditRuleStore(): StrictAuditRuleStore {
    return MemoryStrictAuditRuleStore(mutableMapOf<String, StrictAuditRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAuditRuleStore(
    var inner: StrictAuditRuleStore,
    var cache: TtlCache<StrictAuditRule>,
    var hits: Int,
    var misses: Int,
) : StrictAuditRuleStore {
    override fun find(code: String): StrictAuditRule? {
        val hit = (cache.get(code) as StrictAuditRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAuditRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAuditRule> = inner.list()
}

fun newCachedStrictAuditRuleStore(inner: StrictAuditRuleStore, ttl: Int, clock: Clock): StrictAuditRuleStore {
    return CachedStrictAuditRuleStore(
        inner = inner,
        cache = newTtlCache<StrictAuditRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict audit rules. Empty fields match all. */
data class StrictAuditRuleQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAuditRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAuditRule>): MutableList<StrictAuditRule> {
        val matched = filter(items) { candidate: StrictAuditRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAuditRules(store: StrictAuditRuleStore, query: StrictAuditRuleQuery): MutableList<StrictAuditRule> {
    return query.apply(store.list())
}
