package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt policies by code. */
interface AttemptPolicyStore {
    fun find(code: String): AttemptPolicy?
    fun save(item: AttemptPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptPolicyStore(
    var items: MutableMap<String, AttemptPolicy>,
    var order: MutableList<String>,
) : AttemptPolicyStore {
    override fun find(code: String): AttemptPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptPolicy) {
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

    override fun list(): MutableList<AttemptPolicy> {
        val out = mutableListOf<AttemptPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptPolicyStore(): AttemptPolicyStore {
    return MemoryAttemptPolicyStore(mutableMapOf<String, AttemptPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptPolicyStore(
    var inner: AttemptPolicyStore,
    var cache: TtlCache<AttemptPolicy>,
    var hits: Int,
    var misses: Int,
) : AttemptPolicyStore {
    override fun find(code: String): AttemptPolicy? {
        val hit = (cache.get(code) as AttemptPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptPolicy> = inner.list()
}

fun newCachedAttemptPolicyStore(inner: AttemptPolicyStore, ttl: Int, clock: Clock): AttemptPolicyStore {
    return CachedAttemptPolicyStore(
        inner = inner,
        cache = newTtlCache<AttemptPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt policies. Empty fields match all. */
data class AttemptPolicyQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptPolicy>): MutableList<AttemptPolicy> {
        val matched = filter(items) { candidate: AttemptPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptPolicies(store: AttemptPolicyStore, query: AttemptPolicyQuery): MutableList<AttemptPolicy> {
    return query.apply(store.list())
}
