package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker requests by code. */
interface BrokerRequestStore {
    fun find(code: String): BrokerRequest?
    fun save(item: BrokerRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerRequestStore(
    var items: MutableMap<String, BrokerRequest>,
    var order: MutableList<String>,
) : BrokerRequestStore {
    override fun find(code: String): BrokerRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerRequest) {
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

    override fun list(): MutableList<BrokerRequest> {
        val out = mutableListOf<BrokerRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerRequestStore(): BrokerRequestStore {
    return MemoryBrokerRequestStore(mutableMapOf<String, BrokerRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerRequestStore(
    var inner: BrokerRequestStore,
    var cache: TtlCache<BrokerRequest>,
    var hits: Int,
    var misses: Int,
) : BrokerRequestStore {
    override fun find(code: String): BrokerRequest? {
        val hit = (cache.get(code) as BrokerRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerRequest> = inner.list()
}

fun newCachedBrokerRequestStore(inner: BrokerRequestStore, ttl: Int, clock: Clock): BrokerRequestStore {
    return CachedBrokerRequestStore(
        inner = inner,
        cache = newTtlCache<BrokerRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker requests. Empty fields match all. */
data class BrokerRequestQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BrokerRequest>): MutableList<BrokerRequest> {
        val matched = filter(items) { candidate: BrokerRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerRequests(store: BrokerRequestStore, query: BrokerRequestQuery): MutableList<BrokerRequest> {
    return query.apply(store.list())
}
