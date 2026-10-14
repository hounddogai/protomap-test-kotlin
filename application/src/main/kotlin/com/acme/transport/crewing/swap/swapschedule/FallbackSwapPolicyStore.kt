package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback swap policies by code. */
interface FallbackSwapPolicyStore {
    fun find(code: String): FallbackSwapPolicy?
    fun save(item: FallbackSwapPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSwapPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSwapPolicyStore(
    var items: MutableMap<String, FallbackSwapPolicy>,
    var order: MutableList<String>,
) : FallbackSwapPolicyStore {
    override fun find(code: String): FallbackSwapPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSwapPolicy) {
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

    override fun list(): MutableList<FallbackSwapPolicy> {
        val out = mutableListOf<FallbackSwapPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSwapPolicyStore(): FallbackSwapPolicyStore {
    return MemoryFallbackSwapPolicyStore(mutableMapOf<String, FallbackSwapPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSwapPolicyStore(
    var inner: FallbackSwapPolicyStore,
    var cache: TtlCache<FallbackSwapPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackSwapPolicyStore {
    override fun find(code: String): FallbackSwapPolicy? {
        val hit = (cache.get(code) as FallbackSwapPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSwapPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSwapPolicy> = inner.list()
}

fun newCachedFallbackSwapPolicyStore(inner: FallbackSwapPolicyStore, ttl: Int, clock: Clock): FallbackSwapPolicyStore {
    return CachedFallbackSwapPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackSwapPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback swap policies. Empty fields match all. */
data class FallbackSwapPolicyQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSwapPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackSwapPolicy>): MutableList<FallbackSwapPolicy> {
        val matched = filter(items) { candidate: FallbackSwapPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSwapPolicies(store: FallbackSwapPolicyStore, query: FallbackSwapPolicyQuery): MutableList<FallbackSwapPolicy> {
    return query.apply(store.list())
}
