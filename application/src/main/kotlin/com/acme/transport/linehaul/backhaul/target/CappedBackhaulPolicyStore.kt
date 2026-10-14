package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped backhaul policies by code. */
interface CappedBackhaulPolicyStore {
    fun find(code: String): CappedBackhaulPolicy?
    fun save(item: CappedBackhaulPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBackhaulPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBackhaulPolicyStore(
    var items: MutableMap<String, CappedBackhaulPolicy>,
    var order: MutableList<String>,
) : CappedBackhaulPolicyStore {
    override fun find(code: String): CappedBackhaulPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBackhaulPolicy) {
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

    override fun list(): MutableList<CappedBackhaulPolicy> {
        val out = mutableListOf<CappedBackhaulPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBackhaulPolicyStore(): CappedBackhaulPolicyStore {
    return MemoryCappedBackhaulPolicyStore(
        mutableMapOf<String, CappedBackhaulPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBackhaulPolicyStore(
    var inner: CappedBackhaulPolicyStore,
    var cache: TtlCache<CappedBackhaulPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedBackhaulPolicyStore {
    override fun find(code: String): CappedBackhaulPolicy? {
        val hit = (cache.get(code) as CappedBackhaulPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBackhaulPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBackhaulPolicy> = inner.list()
}

fun newCachedCappedBackhaulPolicyStore(inner: CappedBackhaulPolicyStore, ttl: Int, clock: Clock): CappedBackhaulPolicyStore {
    return CachedCappedBackhaulPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedBackhaulPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped backhaul policies. Empty fields match all. */
data class CappedBackhaulPolicyQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBackhaulPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CappedBackhaulPolicy>): MutableList<CappedBackhaulPolicy> {
        val matched = filter(items) { candidate: CappedBackhaulPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBackhaulPolicies(store: CappedBackhaulPolicyStore, query: CappedBackhaulPolicyQuery): MutableList<CappedBackhaulPolicy> {
    return query.apply(store.list())
}
