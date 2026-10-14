package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rate card policies by code. */
interface RollingRateCardPolicyStore {
    fun find(code: String): RollingRateCardPolicy?
    fun save(item: RollingRateCardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRateCardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRateCardPolicyStore(
    var items: MutableMap<String, RollingRateCardPolicy>,
    var order: MutableList<String>,
) : RollingRateCardPolicyStore {
    override fun find(code: String): RollingRateCardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRateCardPolicy) {
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

    override fun list(): MutableList<RollingRateCardPolicy> {
        val out = mutableListOf<RollingRateCardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRateCardPolicyStore(): RollingRateCardPolicyStore {
    return MemoryRollingRateCardPolicyStore(
        mutableMapOf<String, RollingRateCardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRateCardPolicyStore(
    var inner: RollingRateCardPolicyStore,
    var cache: TtlCache<RollingRateCardPolicy>,
    var hits: Int,
    var misses: Int,
) : RollingRateCardPolicyStore {
    override fun find(code: String): RollingRateCardPolicy? {
        val hit = (cache.get(code) as RollingRateCardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRateCardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRateCardPolicy> = inner.list()
}

fun newCachedRollingRateCardPolicyStore(inner: RollingRateCardPolicyStore, ttl: Int, clock: Clock): RollingRateCardPolicyStore {
    return CachedRollingRateCardPolicyStore(
        inner = inner,
        cache = newTtlCache<RollingRateCardPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rate card policies. Empty fields match all. */
data class RollingRateCardPolicyQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRateCardPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRateCardPolicy>): MutableList<RollingRateCardPolicy> {
        val matched = filter(items) { candidate: RollingRateCardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRateCardPolicies(store: RollingRateCardPolicyStore, query: RollingRateCardPolicyQuery): MutableList<RollingRateCardPolicy> {
    return query.apply(store.list())
}
