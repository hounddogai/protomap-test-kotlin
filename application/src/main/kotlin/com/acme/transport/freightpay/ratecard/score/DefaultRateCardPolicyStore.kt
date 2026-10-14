package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rate card policies by code. */
interface DefaultRateCardPolicyStore {
    fun find(code: String): DefaultRateCardPolicy?
    fun save(item: DefaultRateCardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRateCardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRateCardPolicyStore(
    var items: MutableMap<String, DefaultRateCardPolicy>,
    var order: MutableList<String>,
) : DefaultRateCardPolicyStore {
    override fun find(code: String): DefaultRateCardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRateCardPolicy) {
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

    override fun list(): MutableList<DefaultRateCardPolicy> {
        val out = mutableListOf<DefaultRateCardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRateCardPolicyStore(): DefaultRateCardPolicyStore {
    return MemoryDefaultRateCardPolicyStore(
        mutableMapOf<String, DefaultRateCardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRateCardPolicyStore(
    var inner: DefaultRateCardPolicyStore,
    var cache: TtlCache<DefaultRateCardPolicy>,
    var hits: Int,
    var misses: Int,
) : DefaultRateCardPolicyStore {
    override fun find(code: String): DefaultRateCardPolicy? {
        val hit = (cache.get(code) as DefaultRateCardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRateCardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRateCardPolicy> = inner.list()
}

fun newCachedDefaultRateCardPolicyStore(inner: DefaultRateCardPolicyStore, ttl: Int, clock: Clock): DefaultRateCardPolicyStore {
    return CachedDefaultRateCardPolicyStore(
        inner = inner,
        cache = newTtlCache<DefaultRateCardPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rate card policies. Empty fields match all. */
data class DefaultRateCardPolicyQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRateCardPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRateCardPolicy>): MutableList<DefaultRateCardPolicy> {
        val matched = filter(items) { candidate: DefaultRateCardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRateCardPolicies(store: DefaultRateCardPolicyStore, query: DefaultRateCardPolicyQuery): MutableList<DefaultRateCardPolicy> {
    return query.apply(store.list())
}
