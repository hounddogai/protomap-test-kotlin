package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker policies by code. */
interface BrokerPolicyStore {
    fun find(code: String): BrokerPolicy?
    fun save(item: BrokerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerPolicyStore(
    var items: MutableMap<String, BrokerPolicy>,
    var order: MutableList<String>,
) : BrokerPolicyStore {
    override fun find(code: String): BrokerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerPolicy) {
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

    override fun list(): MutableList<BrokerPolicy> {
        val out = mutableListOf<BrokerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerPolicyStore(): BrokerPolicyStore {
    return MemoryBrokerPolicyStore(mutableMapOf<String, BrokerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerPolicyStore(
    var inner: BrokerPolicyStore,
    var cache: TtlCache<BrokerPolicy>,
    var hits: Int,
    var misses: Int,
) : BrokerPolicyStore {
    override fun find(code: String): BrokerPolicy? {
        val hit = (cache.get(code) as BrokerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerPolicy> = inner.list()
}

fun newCachedBrokerPolicyStore(inner: BrokerPolicyStore, ttl: Int, clock: Clock): BrokerPolicyStore {
    return CachedBrokerPolicyStore(
        inner = inner,
        cache = newTtlCache<BrokerPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker policies. Empty fields match all. */
data class BrokerPolicyQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerPolicy): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerPolicy>): MutableList<BrokerPolicy> {
        val matched = filter(items) { candidate: BrokerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerPolicies(store: BrokerPolicyStore, query: BrokerPolicyQuery): MutableList<BrokerPolicy> {
    return query.apply(store.list())
}
