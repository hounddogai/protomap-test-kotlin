package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive idling policies by code. */
interface AdaptiveIdlingPolicyStore {
    fun find(code: String): AdaptiveIdlingPolicy?
    fun save(item: AdaptiveIdlingPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveIdlingPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveIdlingPolicyStore(
    var items: MutableMap<String, AdaptiveIdlingPolicy>,
    var order: MutableList<String>,
) : AdaptiveIdlingPolicyStore {
    override fun find(code: String): AdaptiveIdlingPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveIdlingPolicy) {
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

    override fun list(): MutableList<AdaptiveIdlingPolicy> {
        val out = mutableListOf<AdaptiveIdlingPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveIdlingPolicyStore(): AdaptiveIdlingPolicyStore {
    return MemoryAdaptiveIdlingPolicyStore(
        mutableMapOf<String, AdaptiveIdlingPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveIdlingPolicyStore(
    var inner: AdaptiveIdlingPolicyStore,
    var cache: TtlCache<AdaptiveIdlingPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveIdlingPolicyStore {
    override fun find(code: String): AdaptiveIdlingPolicy? {
        val hit = (cache.get(code) as AdaptiveIdlingPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveIdlingPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveIdlingPolicy> = inner.list()
}

fun newCachedAdaptiveIdlingPolicyStore(inner: AdaptiveIdlingPolicyStore, ttl: Int, clock: Clock): AdaptiveIdlingPolicyStore {
    return CachedAdaptiveIdlingPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveIdlingPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive idling policies. Empty fields match all. */
data class AdaptiveIdlingPolicyQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveIdlingPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveIdlingPolicy>): MutableList<AdaptiveIdlingPolicy> {
        val matched = filter(items) { candidate: AdaptiveIdlingPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveIdlingPolicies(store: AdaptiveIdlingPolicyStore, query: AdaptiveIdlingPolicyQuery): MutableList<AdaptiveIdlingPolicy> {
    return query.apply(store.list())
}
