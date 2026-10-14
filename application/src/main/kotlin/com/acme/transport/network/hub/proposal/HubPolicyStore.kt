package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub policies by code. */
interface HubPolicyStore {
    fun find(code: String): HubPolicy?
    fun save(item: HubPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubPolicyStore(
    var items: MutableMap<String, HubPolicy>,
    var order: MutableList<String>,
) : HubPolicyStore {
    override fun find(code: String): HubPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubPolicy) {
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

    override fun list(): MutableList<HubPolicy> {
        val out = mutableListOf<HubPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubPolicyStore(): HubPolicyStore {
    return MemoryHubPolicyStore(mutableMapOf<String, HubPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubPolicyStore(
    var inner: HubPolicyStore,
    var cache: TtlCache<HubPolicy>,
    var hits: Int,
    var misses: Int,
) : HubPolicyStore {
    override fun find(code: String): HubPolicy? {
        val hit = (cache.get(code) as HubPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubPolicy> = inner.list()
}

fun newCachedHubPolicyStore(inner: HubPolicyStore, ttl: Int, clock: Clock): HubPolicyStore {
    return CachedHubPolicyStore(
        inner = inner,
        cache = newTtlCache<HubPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub policies. Empty fields match all. */
data class HubPolicyQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubPolicy>): MutableList<HubPolicy> {
        val matched = filter(items) { candidate: HubPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubPolicies(store: HubPolicyStore, query: HubPolicyQuery): MutableList<HubPolicy> {
    return query.apply(store.list())
}
