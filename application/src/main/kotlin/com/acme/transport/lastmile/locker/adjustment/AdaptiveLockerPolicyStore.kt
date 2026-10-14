package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker policies by code. */
interface AdaptiveLockerPolicyStore {
    fun find(code: String): AdaptiveLockerPolicy?
    fun save(item: AdaptiveLockerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerPolicyStore(
    var items: MutableMap<String, AdaptiveLockerPolicy>,
    var order: MutableList<String>,
) : AdaptiveLockerPolicyStore {
    override fun find(code: String): AdaptiveLockerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerPolicy) {
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

    override fun list(): MutableList<AdaptiveLockerPolicy> {
        val out = mutableListOf<AdaptiveLockerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerPolicyStore(): AdaptiveLockerPolicyStore {
    return MemoryAdaptiveLockerPolicyStore(
        mutableMapOf<String, AdaptiveLockerPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerPolicyStore(
    var inner: AdaptiveLockerPolicyStore,
    var cache: TtlCache<AdaptiveLockerPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerPolicyStore {
    override fun find(code: String): AdaptiveLockerPolicy? {
        val hit = (cache.get(code) as AdaptiveLockerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerPolicy> = inner.list()
}

fun newCachedAdaptiveLockerPolicyStore(inner: AdaptiveLockerPolicyStore, ttl: Int, clock: Clock): AdaptiveLockerPolicyStore {
    return CachedAdaptiveLockerPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker policies. Empty fields match all. */
data class AdaptiveLockerPolicyQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLockerPolicy>): MutableList<AdaptiveLockerPolicy> {
        val matched = filter(items) { candidate: AdaptiveLockerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerPolicies(store: AdaptiveLockerPolicyStore, query: AdaptiveLockerPolicyQuery): MutableList<AdaptiveLockerPolicy> {
    return query.apply(store.list())
}
