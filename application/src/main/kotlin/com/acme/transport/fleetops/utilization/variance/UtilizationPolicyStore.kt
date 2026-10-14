package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization policies by code. */
interface UtilizationPolicyStore {
    fun find(code: String): UtilizationPolicy?
    fun save(item: UtilizationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationPolicyStore(
    var items: MutableMap<String, UtilizationPolicy>,
    var order: MutableList<String>,
) : UtilizationPolicyStore {
    override fun find(code: String): UtilizationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationPolicy) {
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

    override fun list(): MutableList<UtilizationPolicy> {
        val out = mutableListOf<UtilizationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationPolicyStore(): UtilizationPolicyStore {
    return MemoryUtilizationPolicyStore(mutableMapOf<String, UtilizationPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationPolicyStore(
    var inner: UtilizationPolicyStore,
    var cache: TtlCache<UtilizationPolicy>,
    var hits: Int,
    var misses: Int,
) : UtilizationPolicyStore {
    override fun find(code: String): UtilizationPolicy? {
        val hit = (cache.get(code) as UtilizationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationPolicy> = inner.list()
}

fun newCachedUtilizationPolicyStore(inner: UtilizationPolicyStore, ttl: Int, clock: Clock): UtilizationPolicyStore {
    return CachedUtilizationPolicyStore(
        inner = inner,
        cache = newTtlCache<UtilizationPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization policies. Empty fields match all. */
data class UtilizationPolicyQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationPolicy>): MutableList<UtilizationPolicy> {
        val matched = filter(items) { candidate: UtilizationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationPolicies(store: UtilizationPolicyStore, query: UtilizationPolicyQuery): MutableList<UtilizationPolicy> {
    return query.apply(store.list())
}
