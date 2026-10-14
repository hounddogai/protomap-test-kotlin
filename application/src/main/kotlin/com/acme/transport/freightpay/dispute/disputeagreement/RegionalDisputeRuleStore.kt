package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional dispute rules by code. */
interface RegionalDisputeRuleStore {
    fun find(code: String): RegionalDisputeRule?
    fun save(item: RegionalDisputeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDisputeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDisputeRuleStore(
    var items: MutableMap<String, RegionalDisputeRule>,
    var order: MutableList<String>,
) : RegionalDisputeRuleStore {
    override fun find(code: String): RegionalDisputeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDisputeRule) {
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

    override fun list(): MutableList<RegionalDisputeRule> {
        val out = mutableListOf<RegionalDisputeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDisputeRuleStore(): RegionalDisputeRuleStore {
    return MemoryRegionalDisputeRuleStore(mutableMapOf<String, RegionalDisputeRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDisputeRuleStore(
    var inner: RegionalDisputeRuleStore,
    var cache: TtlCache<RegionalDisputeRule>,
    var hits: Int,
    var misses: Int,
) : RegionalDisputeRuleStore {
    override fun find(code: String): RegionalDisputeRule? {
        val hit = (cache.get(code) as RegionalDisputeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDisputeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDisputeRule> = inner.list()
}

fun newCachedRegionalDisputeRuleStore(inner: RegionalDisputeRuleStore, ttl: Int, clock: Clock): RegionalDisputeRuleStore {
    return CachedRegionalDisputeRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalDisputeRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dispute rules. Empty fields match all. */
data class RegionalDisputeRuleQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDisputeRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDisputeRule>): MutableList<RegionalDisputeRule> {
        val matched = filter(items) { candidate: RegionalDisputeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDisputeRules(store: RegionalDisputeRuleStore, query: RegionalDisputeRuleQuery): MutableList<RegionalDisputeRule> {
    return query.apply(store.list())
}
