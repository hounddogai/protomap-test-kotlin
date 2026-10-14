package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold requests by code. */
interface HoldRequestStore {
    fun find(code: String): HoldRequest?
    fun save(item: HoldRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldRequestStore(
    var items: MutableMap<String, HoldRequest>,
    var order: MutableList<String>,
) : HoldRequestStore {
    override fun find(code: String): HoldRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldRequest) {
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

    override fun list(): MutableList<HoldRequest> {
        val out = mutableListOf<HoldRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldRequestStore(): HoldRequestStore {
    return MemoryHoldRequestStore(mutableMapOf<String, HoldRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldRequestStore(
    var inner: HoldRequestStore,
    var cache: TtlCache<HoldRequest>,
    var hits: Int,
    var misses: Int,
) : HoldRequestStore {
    override fun find(code: String): HoldRequest? {
        val hit = (cache.get(code) as HoldRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldRequest> = inner.list()
}

fun newCachedHoldRequestStore(inner: HoldRequestStore, ttl: Int, clock: Clock): HoldRequestStore {
    return CachedHoldRequestStore(
        inner = inner,
        cache = newTtlCache<HoldRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold requests. Empty fields match all. */
data class HoldRequestQuery(
    var region: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldRequest>): MutableList<HoldRequest> {
        val matched = filter(items) { candidate: HoldRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldRequests(store: HoldRequestStore, query: HoldRequestQuery): MutableList<HoldRequest> {
    return query.apply(store.list())
}
