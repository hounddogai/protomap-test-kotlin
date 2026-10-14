package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation policies by code. */
interface StrictViolationPolicyStore {
    fun find(code: String): StrictViolationPolicy?
    fun save(item: StrictViolationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationPolicyStore(
    var items: MutableMap<String, StrictViolationPolicy>,
    var order: MutableList<String>,
) : StrictViolationPolicyStore {
    override fun find(code: String): StrictViolationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationPolicy) {
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

    override fun list(): MutableList<StrictViolationPolicy> {
        val out = mutableListOf<StrictViolationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationPolicyStore(): StrictViolationPolicyStore {
    return MemoryStrictViolationPolicyStore(
        mutableMapOf<String, StrictViolationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationPolicyStore(
    var inner: StrictViolationPolicyStore,
    var cache: TtlCache<StrictViolationPolicy>,
    var hits: Int,
    var misses: Int,
) : StrictViolationPolicyStore {
    override fun find(code: String): StrictViolationPolicy? {
        val hit = (cache.get(code) as StrictViolationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationPolicy> = inner.list()
}

fun newCachedStrictViolationPolicyStore(inner: StrictViolationPolicyStore, ttl: Int, clock: Clock): StrictViolationPolicyStore {
    return CachedStrictViolationPolicyStore(
        inner = inner,
        cache = newTtlCache<StrictViolationPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation policies. Empty fields match all. */
data class StrictViolationPolicyQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictViolationPolicy>): MutableList<StrictViolationPolicy> {
        val matched = filter(items) { candidate: StrictViolationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationPolicies(store: StrictViolationPolicyStore, query: StrictViolationPolicyQuery): MutableList<StrictViolationPolicy> {
    return query.apply(store.list())
}
