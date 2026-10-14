package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped dispute policies by code. */
interface CappedDisputePolicyStore {
    fun find(code: String): CappedDisputePolicy?
    fun save(item: CappedDisputePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDisputePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDisputePolicyStore(
    var items: MutableMap<String, CappedDisputePolicy>,
    var order: MutableList<String>,
) : CappedDisputePolicyStore {
    override fun find(code: String): CappedDisputePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDisputePolicy) {
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

    override fun list(): MutableList<CappedDisputePolicy> {
        val out = mutableListOf<CappedDisputePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDisputePolicyStore(): CappedDisputePolicyStore {
    return MemoryCappedDisputePolicyStore(mutableMapOf<String, CappedDisputePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDisputePolicyStore(
    var inner: CappedDisputePolicyStore,
    var cache: TtlCache<CappedDisputePolicy>,
    var hits: Int,
    var misses: Int,
) : CappedDisputePolicyStore {
    override fun find(code: String): CappedDisputePolicy? {
        val hit = (cache.get(code) as CappedDisputePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDisputePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDisputePolicy> = inner.list()
}

fun newCachedCappedDisputePolicyStore(inner: CappedDisputePolicyStore, ttl: Int, clock: Clock): CappedDisputePolicyStore {
    return CachedCappedDisputePolicyStore(
        inner = inner,
        cache = newTtlCache<CappedDisputePolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dispute policies. Empty fields match all. */
data class CappedDisputePolicyQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDisputePolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CappedDisputePolicy>): MutableList<CappedDisputePolicy> {
        val matched = filter(items) { candidate: CappedDisputePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDisputePolicies(store: CappedDisputePolicyStore, query: CappedDisputePolicyQuery): MutableList<CappedDisputePolicy> {
    return query.apply(store.list())
}
