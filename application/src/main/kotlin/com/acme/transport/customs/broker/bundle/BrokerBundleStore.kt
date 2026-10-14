package com.acme.transport.customs.broker.bundle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker bundles by code. */
interface BrokerBundleStore {
    fun find(code: String): BrokerBundle?
    fun save(item: BrokerBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerBundleStore(
    var items: MutableMap<String, BrokerBundle>,
    var order: MutableList<String>,
) : BrokerBundleStore {
    override fun find(code: String): BrokerBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerBundle) {
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

    override fun list(): MutableList<BrokerBundle> {
        val out = mutableListOf<BrokerBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerBundleStore(): BrokerBundleStore {
    return MemoryBrokerBundleStore(mutableMapOf<String, BrokerBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerBundleStore(
    var inner: BrokerBundleStore,
    var cache: TtlCache<BrokerBundle>,
    var hits: Int,
    var misses: Int,
) : BrokerBundleStore {
    override fun find(code: String): BrokerBundle? {
        val hit = (cache.get(code) as BrokerBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerBundle> = inner.list()
}

fun newCachedBrokerBundleStore(inner: BrokerBundleStore, ttl: Int, clock: Clock): BrokerBundleStore {
    return CachedBrokerBundleStore(
        inner = inner,
        cache = newTtlCache<BrokerBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker bundles. Empty fields match all. */
data class BrokerBundleQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerBundle): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerBundle>): MutableList<BrokerBundle> {
        val matched = filter(items) { candidate: BrokerBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerBundles(store: BrokerBundleStore, query: BrokerBundleQuery): MutableList<BrokerBundle> {
    return query.apply(store.list())
}
