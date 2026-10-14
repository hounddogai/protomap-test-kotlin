package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation policies by code. */
interface ViolationPolicyStore {
    fun find(code: String): ViolationPolicy?
    fun save(item: ViolationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationPolicyStore(
    var items: MutableMap<String, ViolationPolicy>,
    var order: MutableList<String>,
) : ViolationPolicyStore {
    override fun find(code: String): ViolationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationPolicy) {
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

    override fun list(): MutableList<ViolationPolicy> {
        val out = mutableListOf<ViolationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationPolicyStore(): ViolationPolicyStore {
    return MemoryViolationPolicyStore(mutableMapOf<String, ViolationPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationPolicyStore(
    var inner: ViolationPolicyStore,
    var cache: TtlCache<ViolationPolicy>,
    var hits: Int,
    var misses: Int,
) : ViolationPolicyStore {
    override fun find(code: String): ViolationPolicy? {
        val hit = (cache.get(code) as ViolationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationPolicy> = inner.list()
}

fun newCachedViolationPolicyStore(inner: ViolationPolicyStore, ttl: Int, clock: Clock): ViolationPolicyStore {
    return CachedViolationPolicyStore(
        inner = inner,
        cache = newTtlCache<ViolationPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation policies. Empty fields match all. */
data class ViolationPolicyQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationPolicy>): MutableList<ViolationPolicy> {
        val matched = filter(items) { candidate: ViolationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationPolicies(store: ViolationPolicyStore, query: ViolationPolicyQuery): MutableList<ViolationPolicy> {
    return query.apply(store.list())
}
