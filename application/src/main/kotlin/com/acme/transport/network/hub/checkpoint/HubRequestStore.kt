package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub requests by code. */
interface HubRequestStore {
    fun find(code: String): HubRequest?
    fun save(item: HubRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubRequestStore(
    var items: MutableMap<String, HubRequest>,
    var order: MutableList<String>,
) : HubRequestStore {
    override fun find(code: String): HubRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubRequest) {
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

    override fun list(): MutableList<HubRequest> {
        val out = mutableListOf<HubRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubRequestStore(): HubRequestStore {
    return MemoryHubRequestStore(mutableMapOf<String, HubRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubRequestStore(
    var inner: HubRequestStore,
    var cache: TtlCache<HubRequest>,
    var hits: Int,
    var misses: Int,
) : HubRequestStore {
    override fun find(code: String): HubRequest? {
        val hit = (cache.get(code) as HubRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubRequest> = inner.list()
}

fun newCachedHubRequestStore(inner: HubRequestStore, ttl: Int, clock: Clock): HubRequestStore {
    return CachedHubRequestStore(
        inner = inner,
        cache = newTtlCache<HubRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub requests. Empty fields match all. */
data class HubRequestQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<HubRequest>): MutableList<HubRequest> {
        val matched = filter(items) { candidate: HubRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubRequests(store: HubRequestStore, query: HubRequestQuery): MutableList<HubRequest> {
    return query.apply(store.list())
}
