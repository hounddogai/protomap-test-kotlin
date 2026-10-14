package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker overrides by code. */
interface BrokerOverrideStore {
    fun find(code: String): BrokerOverride?
    fun save(item: BrokerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerOverrideStore(
    var items: MutableMap<String, BrokerOverride>,
    var order: MutableList<String>,
) : BrokerOverrideStore {
    override fun find(code: String): BrokerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerOverride) {
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

    override fun list(): MutableList<BrokerOverride> {
        val out = mutableListOf<BrokerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerOverrideStore(): BrokerOverrideStore {
    return MemoryBrokerOverrideStore(mutableMapOf<String, BrokerOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerOverrideStore(
    var inner: BrokerOverrideStore,
    var cache: TtlCache<BrokerOverride>,
    var hits: Int,
    var misses: Int,
) : BrokerOverrideStore {
    override fun find(code: String): BrokerOverride? {
        val hit = (cache.get(code) as BrokerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerOverride> = inner.list()
}

fun newCachedBrokerOverrideStore(inner: BrokerOverrideStore, ttl: Int, clock: Clock): BrokerOverrideStore {
    return CachedBrokerOverrideStore(
        inner = inner,
        cache = newTtlCache<BrokerOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker overrides. Empty fields match all. */
data class BrokerOverrideQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerOverride>): MutableList<BrokerOverride> {
        val matched = filter(items) { candidate: BrokerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerOverrides(store: BrokerOverrideStore, query: BrokerOverrideQuery): MutableList<BrokerOverride> {
    return query.apply(store.list())
}
