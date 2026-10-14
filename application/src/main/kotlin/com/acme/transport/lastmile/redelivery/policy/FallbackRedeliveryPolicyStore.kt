package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback redelivery policies by code. */
interface FallbackRedeliveryPolicyStore {
    fun find(code: String): FallbackRedeliveryPolicy?
    fun save(item: FallbackRedeliveryPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRedeliveryPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRedeliveryPolicyStore(
    var items: MutableMap<String, FallbackRedeliveryPolicy>,
    var order: MutableList<String>,
) : FallbackRedeliveryPolicyStore {
    override fun find(code: String): FallbackRedeliveryPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRedeliveryPolicy) {
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

    override fun list(): MutableList<FallbackRedeliveryPolicy> {
        val out = mutableListOf<FallbackRedeliveryPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRedeliveryPolicyStore(): FallbackRedeliveryPolicyStore {
    return MemoryFallbackRedeliveryPolicyStore(
        mutableMapOf<String, FallbackRedeliveryPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRedeliveryPolicyStore(
    var inner: FallbackRedeliveryPolicyStore,
    var cache: TtlCache<FallbackRedeliveryPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackRedeliveryPolicyStore {
    override fun find(code: String): FallbackRedeliveryPolicy? {
        val hit = (cache.get(code) as FallbackRedeliveryPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRedeliveryPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRedeliveryPolicy> = inner.list()
}

fun newCachedFallbackRedeliveryPolicyStore(inner: FallbackRedeliveryPolicyStore, ttl: Int, clock: Clock): FallbackRedeliveryPolicyStore {
    return CachedFallbackRedeliveryPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackRedeliveryPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback redelivery policies. Empty fields match all. */
data class FallbackRedeliveryPolicyQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRedeliveryPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRedeliveryPolicy>): MutableList<FallbackRedeliveryPolicy> {
        val matched = filter(items) { candidate: FallbackRedeliveryPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRedeliveryPolicies(store: FallbackRedeliveryPolicyStore, query: FallbackRedeliveryPolicyQuery): MutableList<FallbackRedeliveryPolicy> {
    return query.apply(store.list())
}
