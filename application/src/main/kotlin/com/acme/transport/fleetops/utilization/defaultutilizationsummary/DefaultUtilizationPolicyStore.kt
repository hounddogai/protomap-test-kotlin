package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default utilization policies by code. */
interface DefaultUtilizationPolicyStore {
    fun find(code: String): DefaultUtilizationPolicy?
    fun save(item: DefaultUtilizationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultUtilizationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultUtilizationPolicyStore(
    var items: MutableMap<String, DefaultUtilizationPolicy>,
    var order: MutableList<String>,
) : DefaultUtilizationPolicyStore {
    override fun find(code: String): DefaultUtilizationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultUtilizationPolicy) {
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

    override fun list(): MutableList<DefaultUtilizationPolicy> {
        val out = mutableListOf<DefaultUtilizationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultUtilizationPolicyStore(): DefaultUtilizationPolicyStore {
    return MemoryDefaultUtilizationPolicyStore(
        mutableMapOf<String, DefaultUtilizationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultUtilizationPolicyStore(
    var inner: DefaultUtilizationPolicyStore,
    var cache: TtlCache<DefaultUtilizationPolicy>,
    var hits: Int,
    var misses: Int,
) : DefaultUtilizationPolicyStore {
    override fun find(code: String): DefaultUtilizationPolicy? {
        val hit = (cache.get(code) as DefaultUtilizationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultUtilizationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultUtilizationPolicy> = inner.list()
}

fun newCachedDefaultUtilizationPolicyStore(inner: DefaultUtilizationPolicyStore, ttl: Int, clock: Clock): DefaultUtilizationPolicyStore {
    return CachedDefaultUtilizationPolicyStore(
        inner = inner,
        cache = newTtlCache<DefaultUtilizationPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default utilization policies. Empty fields match all. */
data class DefaultUtilizationPolicyQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultUtilizationPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultUtilizationPolicy>): MutableList<DefaultUtilizationPolicy> {
        val matched = filter(items) { candidate: DefaultUtilizationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultUtilizationPolicies(store: DefaultUtilizationPolicyStore, query: DefaultUtilizationPolicyQuery): MutableList<DefaultUtilizationPolicy> {
    return query.apply(store.list())
}
