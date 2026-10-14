package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback attempt policies by code. */
interface FallbackAttemptPolicyStore {
    fun find(code: String): FallbackAttemptPolicy?
    fun save(item: FallbackAttemptPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAttemptPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAttemptPolicyStore(
    var items: MutableMap<String, FallbackAttemptPolicy>,
    var order: MutableList<String>,
) : FallbackAttemptPolicyStore {
    override fun find(code: String): FallbackAttemptPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAttemptPolicy) {
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

    override fun list(): MutableList<FallbackAttemptPolicy> {
        val out = mutableListOf<FallbackAttemptPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAttemptPolicyStore(): FallbackAttemptPolicyStore {
    return MemoryFallbackAttemptPolicyStore(
        mutableMapOf<String, FallbackAttemptPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAttemptPolicyStore(
    var inner: FallbackAttemptPolicyStore,
    var cache: TtlCache<FallbackAttemptPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackAttemptPolicyStore {
    override fun find(code: String): FallbackAttemptPolicy? {
        val hit = (cache.get(code) as FallbackAttemptPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAttemptPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAttemptPolicy> = inner.list()
}

fun newCachedFallbackAttemptPolicyStore(inner: FallbackAttemptPolicyStore, ttl: Int, clock: Clock): FallbackAttemptPolicyStore {
    return CachedFallbackAttemptPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackAttemptPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback attempt policies. Empty fields match all. */
data class FallbackAttemptPolicyQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAttemptPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAttemptPolicy>): MutableList<FallbackAttemptPolicy> {
        val matched = filter(items) { candidate: FallbackAttemptPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAttemptPolicies(store: FallbackAttemptPolicyStore, query: FallbackAttemptPolicyQuery): MutableList<FallbackAttemptPolicy> {
    return query.apply(store.list())
}
