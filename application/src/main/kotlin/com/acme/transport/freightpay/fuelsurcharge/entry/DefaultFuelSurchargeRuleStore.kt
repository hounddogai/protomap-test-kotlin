package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default fuel surcharge rules by code. */
interface DefaultFuelSurchargeRuleStore {
    fun find(code: String): DefaultFuelSurchargeRule?
    fun save(item: DefaultFuelSurchargeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultFuelSurchargeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultFuelSurchargeRuleStore(
    var items: MutableMap<String, DefaultFuelSurchargeRule>,
    var order: MutableList<String>,
) : DefaultFuelSurchargeRuleStore {
    override fun find(code: String): DefaultFuelSurchargeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultFuelSurchargeRule) {
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

    override fun list(): MutableList<DefaultFuelSurchargeRule> {
        val out = mutableListOf<DefaultFuelSurchargeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultFuelSurchargeRuleStore(): DefaultFuelSurchargeRuleStore {
    return MemoryDefaultFuelSurchargeRuleStore(
        mutableMapOf<String, DefaultFuelSurchargeRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultFuelSurchargeRuleStore(
    var inner: DefaultFuelSurchargeRuleStore,
    var cache: TtlCache<DefaultFuelSurchargeRule>,
    var hits: Int,
    var misses: Int,
) : DefaultFuelSurchargeRuleStore {
    override fun find(code: String): DefaultFuelSurchargeRule? {
        val hit = (cache.get(code) as DefaultFuelSurchargeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultFuelSurchargeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultFuelSurchargeRule> = inner.list()
}

fun newCachedDefaultFuelSurchargeRuleStore(inner: DefaultFuelSurchargeRuleStore, ttl: Int, clock: Clock): DefaultFuelSurchargeRuleStore {
    return CachedDefaultFuelSurchargeRuleStore(
        inner = inner,
        cache = newTtlCache<DefaultFuelSurchargeRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default fuel surcharge rules. Empty fields match all. */
data class DefaultFuelSurchargeRuleQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultFuelSurchargeRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultFuelSurchargeRule>): MutableList<DefaultFuelSurchargeRule> {
        val matched = filter(items) { candidate: DefaultFuelSurchargeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultFuelSurchargeRules(store: DefaultFuelSurchargeRuleStore, query: DefaultFuelSurchargeRuleQuery): MutableList<DefaultFuelSurchargeRule> {
    return query.apply(store.list())
}
