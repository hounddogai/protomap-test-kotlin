package com.acme.transport.customs.broker.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker requests by code. */
interface RegionalBrokerRequestStore {
    fun find(code: String): RegionalBrokerRequest?
    fun save(item: RegionalBrokerRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerRequestStore(
    var items: MutableMap<String, RegionalBrokerRequest>,
    var order: MutableList<String>,
) : RegionalBrokerRequestStore {
    override fun find(code: String): RegionalBrokerRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerRequest) {
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

    override fun list(): MutableList<RegionalBrokerRequest> {
        val out = mutableListOf<RegionalBrokerRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerRequestStore(): RegionalBrokerRequestStore {
    return MemoryRegionalBrokerRequestStore(
        mutableMapOf<String, RegionalBrokerRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerRequestStore(
    var inner: RegionalBrokerRequestStore,
    var cache: TtlCache<RegionalBrokerRequest>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerRequestStore {
    override fun find(code: String): RegionalBrokerRequest? {
        val hit = (cache.get(code) as RegionalBrokerRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerRequest> = inner.list()
}

fun newCachedRegionalBrokerRequestStore(inner: RegionalBrokerRequestStore, ttl: Int, clock: Clock): RegionalBrokerRequestStore {
    return CachedRegionalBrokerRequestStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker requests. Empty fields match all. */
data class RegionalBrokerRequestQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RegionalBrokerRequest>): MutableList<RegionalBrokerRequest> {
        val matched = filter(items) { candidate: RegionalBrokerRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerRequests(store: RegionalBrokerRequestStore, query: RegionalBrokerRequestQuery): MutableList<RegionalBrokerRequest> {
    return query.apply(store.list())
}
