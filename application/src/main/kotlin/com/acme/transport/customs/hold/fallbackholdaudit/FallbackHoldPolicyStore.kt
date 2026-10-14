package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback hold policies by code. */
interface FallbackHoldPolicyStore {
    fun find(code: String): FallbackHoldPolicy?
    fun save(item: FallbackHoldPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackHoldPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackHoldPolicyStore(
    var items: MutableMap<String, FallbackHoldPolicy>,
    var order: MutableList<String>,
) : FallbackHoldPolicyStore {
    override fun find(code: String): FallbackHoldPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackHoldPolicy) {
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

    override fun list(): MutableList<FallbackHoldPolicy> {
        val out = mutableListOf<FallbackHoldPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackHoldPolicyStore(): FallbackHoldPolicyStore {
    return MemoryFallbackHoldPolicyStore(mutableMapOf<String, FallbackHoldPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackHoldPolicyStore(
    var inner: FallbackHoldPolicyStore,
    var cache: TtlCache<FallbackHoldPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackHoldPolicyStore {
    override fun find(code: String): FallbackHoldPolicy? {
        val hit = (cache.get(code) as FallbackHoldPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackHoldPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackHoldPolicy> = inner.list()
}

fun newCachedFallbackHoldPolicyStore(inner: FallbackHoldPolicyStore, ttl: Int, clock: Clock): FallbackHoldPolicyStore {
    return CachedFallbackHoldPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackHoldPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback hold policies. Empty fields match all. */
data class FallbackHoldPolicyQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackHoldPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackHoldPolicy>): MutableList<FallbackHoldPolicy> {
        val matched = filter(items) { candidate: FallbackHoldPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackHoldPolicies(store: FallbackHoldPolicyStore, query: FallbackHoldPolicyQuery): MutableList<FallbackHoldPolicy> {
    return query.apply(store.list())
}
