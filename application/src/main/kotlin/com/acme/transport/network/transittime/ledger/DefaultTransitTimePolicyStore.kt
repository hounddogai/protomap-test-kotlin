package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time policies by code. */
interface DefaultTransitTimePolicyStore {
    fun find(code: String): DefaultTransitTimePolicy?
    fun save(item: DefaultTransitTimePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimePolicyStore(
    var items: MutableMap<String, DefaultTransitTimePolicy>,
    var order: MutableList<String>,
) : DefaultTransitTimePolicyStore {
    override fun find(code: String): DefaultTransitTimePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimePolicy) {
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

    override fun list(): MutableList<DefaultTransitTimePolicy> {
        val out = mutableListOf<DefaultTransitTimePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimePolicyStore(): DefaultTransitTimePolicyStore {
    return MemoryDefaultTransitTimePolicyStore(
        mutableMapOf<String, DefaultTransitTimePolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimePolicyStore(
    var inner: DefaultTransitTimePolicyStore,
    var cache: TtlCache<DefaultTransitTimePolicy>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimePolicyStore {
    override fun find(code: String): DefaultTransitTimePolicy? {
        val hit = (cache.get(code) as DefaultTransitTimePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimePolicy> = inner.list()
}

fun newCachedDefaultTransitTimePolicyStore(inner: DefaultTransitTimePolicyStore, ttl: Int, clock: Clock): DefaultTransitTimePolicyStore {
    return CachedDefaultTransitTimePolicyStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimePolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time policies. Empty fields match all. */
data class DefaultTransitTimePolicyQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimePolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTransitTimePolicy>): MutableList<DefaultTransitTimePolicy> {
        val matched = filter(items) { candidate: DefaultTransitTimePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimePolicies(store: DefaultTransitTimePolicyStore, query: DefaultTransitTimePolicyQuery): MutableList<DefaultTransitTimePolicy> {
    return query.apply(store.list())
}
