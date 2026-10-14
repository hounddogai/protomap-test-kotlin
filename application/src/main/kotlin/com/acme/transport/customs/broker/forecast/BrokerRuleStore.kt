package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker rules by code. */
interface BrokerRuleStore {
    fun find(code: String): BrokerRule?
    fun save(item: BrokerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerRuleStore(
    var items: MutableMap<String, BrokerRule>,
    var order: MutableList<String>,
) : BrokerRuleStore {
    override fun find(code: String): BrokerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerRule) {
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

    override fun list(): MutableList<BrokerRule> {
        val out = mutableListOf<BrokerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerRuleStore(): BrokerRuleStore {
    return MemoryBrokerRuleStore(mutableMapOf<String, BrokerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerRuleStore(
    var inner: BrokerRuleStore,
    var cache: TtlCache<BrokerRule>,
    var hits: Int,
    var misses: Int,
) : BrokerRuleStore {
    override fun find(code: String): BrokerRule? {
        val hit = (cache.get(code) as BrokerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerRule> = inner.list()
}

fun newCachedBrokerRuleStore(inner: BrokerRuleStore, ttl: Int, clock: Clock): BrokerRuleStore {
    return CachedBrokerRuleStore(
        inner = inner,
        cache = newTtlCache<BrokerRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker rules. Empty fields match all. */
data class BrokerRuleQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerRule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<BrokerRule>): MutableList<BrokerRule> {
        val matched = filter(items) { candidate: BrokerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerRules(store: BrokerRuleStore, query: BrokerRuleQuery): MutableList<BrokerRule> {
    return query.apply(store.list())
}
