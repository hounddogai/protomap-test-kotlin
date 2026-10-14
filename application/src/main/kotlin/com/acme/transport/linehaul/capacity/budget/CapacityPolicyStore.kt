package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity policies by code. */
interface CapacityPolicyStore {
    fun find(code: String): CapacityPolicy?
    fun save(item: CapacityPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityPolicyStore(
    var items: MutableMap<String, CapacityPolicy>,
    var order: MutableList<String>,
) : CapacityPolicyStore {
    override fun find(code: String): CapacityPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityPolicy) {
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

    override fun list(): MutableList<CapacityPolicy> {
        val out = mutableListOf<CapacityPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityPolicyStore(): CapacityPolicyStore {
    return MemoryCapacityPolicyStore(mutableMapOf<String, CapacityPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityPolicyStore(
    var inner: CapacityPolicyStore,
    var cache: TtlCache<CapacityPolicy>,
    var hits: Int,
    var misses: Int,
) : CapacityPolicyStore {
    override fun find(code: String): CapacityPolicy? {
        val hit = (cache.get(code) as CapacityPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityPolicy> = inner.list()
}

fun newCachedCapacityPolicyStore(inner: CapacityPolicyStore, ttl: Int, clock: Clock): CapacityPolicyStore {
    return CachedCapacityPolicyStore(
        inner = inner,
        cache = newTtlCache<CapacityPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity policies. Empty fields match all. */
data class CapacityPolicyQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityPolicy>): MutableList<CapacityPolicy> {
        val matched = filter(items) { candidate: CapacityPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityPolicies(store: CapacityPolicyStore, query: CapacityPolicyQuery): MutableList<CapacityPolicy> {
    return query.apply(store.list())
}
