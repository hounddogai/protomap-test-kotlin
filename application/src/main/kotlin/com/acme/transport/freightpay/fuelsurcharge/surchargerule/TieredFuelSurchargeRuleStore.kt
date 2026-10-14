package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel surcharge rules by code. */
interface TieredFuelSurchargeRuleStore {
    fun find(code: String): TieredFuelSurchargeRule?
    fun save(item: TieredFuelSurchargeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelSurchargeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelSurchargeRuleStore(
    var items: MutableMap<String, TieredFuelSurchargeRule>,
    var order: MutableList<String>,
) : TieredFuelSurchargeRuleStore {
    override fun find(code: String): TieredFuelSurchargeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelSurchargeRule) {
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

    override fun list(): MutableList<TieredFuelSurchargeRule> {
        val out = mutableListOf<TieredFuelSurchargeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelSurchargeRuleStore(): TieredFuelSurchargeRuleStore {
    return MemoryTieredFuelSurchargeRuleStore(
        mutableMapOf<String, TieredFuelSurchargeRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelSurchargeRuleStore(
    var inner: TieredFuelSurchargeRuleStore,
    var cache: TtlCache<TieredFuelSurchargeRule>,
    var hits: Int,
    var misses: Int,
) : TieredFuelSurchargeRuleStore {
    override fun find(code: String): TieredFuelSurchargeRule? {
        val hit = (cache.get(code) as TieredFuelSurchargeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelSurchargeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelSurchargeRule> = inner.list()
}

fun newCachedTieredFuelSurchargeRuleStore(inner: TieredFuelSurchargeRuleStore, ttl: Int, clock: Clock): TieredFuelSurchargeRuleStore {
    return CachedTieredFuelSurchargeRuleStore(
        inner = inner,
        cache = newTtlCache<TieredFuelSurchargeRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel surcharge rules. Empty fields match all. */
data class TieredFuelSurchargeRuleQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelSurchargeRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<TieredFuelSurchargeRule>): MutableList<TieredFuelSurchargeRule> {
        val matched = filter(items) { candidate: TieredFuelSurchargeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelSurchargeRules(store: TieredFuelSurchargeRuleStore, query: TieredFuelSurchargeRuleQuery): MutableList<TieredFuelSurchargeRule> {
    return query.apply(store.list())
}
