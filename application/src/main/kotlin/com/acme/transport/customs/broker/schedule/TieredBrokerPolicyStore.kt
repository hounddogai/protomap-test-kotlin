package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered broker policies by code. */
interface TieredBrokerPolicyStore {
    fun find(code: String): TieredBrokerPolicy?
    fun save(item: TieredBrokerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBrokerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBrokerPolicyStore(
    var items: MutableMap<String, TieredBrokerPolicy>,
    var order: MutableList<String>,
) : TieredBrokerPolicyStore {
    override fun find(code: String): TieredBrokerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBrokerPolicy) {
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

    override fun list(): MutableList<TieredBrokerPolicy> {
        val out = mutableListOf<TieredBrokerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBrokerPolicyStore(): TieredBrokerPolicyStore {
    return MemoryTieredBrokerPolicyStore(mutableMapOf<String, TieredBrokerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBrokerPolicyStore(
    var inner: TieredBrokerPolicyStore,
    var cache: TtlCache<TieredBrokerPolicy>,
    var hits: Int,
    var misses: Int,
) : TieredBrokerPolicyStore {
    override fun find(code: String): TieredBrokerPolicy? {
        val hit = (cache.get(code) as TieredBrokerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBrokerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBrokerPolicy> = inner.list()
}

fun newCachedTieredBrokerPolicyStore(inner: TieredBrokerPolicyStore, ttl: Int, clock: Clock): TieredBrokerPolicyStore {
    return CachedTieredBrokerPolicyStore(
        inner = inner,
        cache = newTtlCache<TieredBrokerPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered broker policies. Empty fields match all. */
data class TieredBrokerPolicyQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBrokerPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredBrokerPolicy>): MutableList<TieredBrokerPolicy> {
        val matched = filter(items) { candidate: TieredBrokerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBrokerPolicies(store: TieredBrokerPolicyStore, query: TieredBrokerPolicyQuery): MutableList<TieredBrokerPolicy> {
    return query.apply(store.list())
}
