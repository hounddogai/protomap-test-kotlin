package com.acme.transport.customs.broker.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled broker requests by code. */
interface ScheduledBrokerRequestStore {
    fun find(code: String): ScheduledBrokerRequest?
    fun save(item: ScheduledBrokerRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBrokerRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBrokerRequestStore(
    var items: MutableMap<String, ScheduledBrokerRequest>,
    var order: MutableList<String>,
) : ScheduledBrokerRequestStore {
    override fun find(code: String): ScheduledBrokerRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBrokerRequest) {
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

    override fun list(): MutableList<ScheduledBrokerRequest> {
        val out = mutableListOf<ScheduledBrokerRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBrokerRequestStore(): ScheduledBrokerRequestStore {
    return MemoryScheduledBrokerRequestStore(
        mutableMapOf<String, ScheduledBrokerRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBrokerRequestStore(
    var inner: ScheduledBrokerRequestStore,
    var cache: TtlCache<ScheduledBrokerRequest>,
    var hits: Int,
    var misses: Int,
) : ScheduledBrokerRequestStore {
    override fun find(code: String): ScheduledBrokerRequest? {
        val hit = (cache.get(code) as ScheduledBrokerRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBrokerRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBrokerRequest> = inner.list()
}

fun newCachedScheduledBrokerRequestStore(inner: ScheduledBrokerRequestStore, ttl: Int, clock: Clock): ScheduledBrokerRequestStore {
    return CachedScheduledBrokerRequestStore(
        inner = inner,
        cache = newTtlCache<ScheduledBrokerRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled broker requests. Empty fields match all. */
data class ScheduledBrokerRequestQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBrokerRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledBrokerRequest>): MutableList<ScheduledBrokerRequest> {
        val matched = filter(items) { candidate: ScheduledBrokerRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBrokerRequests(store: ScheduledBrokerRequestStore, query: ScheduledBrokerRequestQuery): MutableList<ScheduledBrokerRequest> {
    return query.apply(store.list())
}
