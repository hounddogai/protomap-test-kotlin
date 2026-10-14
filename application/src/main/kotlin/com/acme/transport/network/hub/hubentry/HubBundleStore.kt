package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub bundles by code. */
interface HubBundleStore {
    fun find(code: String): HubBundle?
    fun save(item: HubBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubBundleStore(
    var items: MutableMap<String, HubBundle>,
    var order: MutableList<String>,
) : HubBundleStore {
    override fun find(code: String): HubBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubBundle) {
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

    override fun list(): MutableList<HubBundle> {
        val out = mutableListOf<HubBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubBundleStore(): HubBundleStore {
    return MemoryHubBundleStore(mutableMapOf<String, HubBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubBundleStore(
    var inner: HubBundleStore,
    var cache: TtlCache<HubBundle>,
    var hits: Int,
    var misses: Int,
) : HubBundleStore {
    override fun find(code: String): HubBundle? {
        val hit = (cache.get(code) as HubBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubBundle> = inner.list()
}

fun newCachedHubBundleStore(inner: HubBundleStore, ttl: Int, clock: Clock): HubBundleStore {
    return CachedHubBundleStore(
        inner = inner,
        cache = newTtlCache<HubBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub bundles. Empty fields match all. */
data class HubBundleQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubBundle>): MutableList<HubBundle> {
        val matched = filter(items) { candidate: HubBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubBundles(store: HubBundleStore, query: HubBundleQuery): MutableList<HubBundle> {
    return query.apply(store.list())
}
