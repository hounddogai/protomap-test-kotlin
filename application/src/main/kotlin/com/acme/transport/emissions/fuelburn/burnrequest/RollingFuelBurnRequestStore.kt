package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn requests by code. */
interface RollingFuelBurnRequestStore {
    fun find(code: String): RollingFuelBurnRequest?
    fun save(item: RollingFuelBurnRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnRequestStore(
    var items: MutableMap<String, RollingFuelBurnRequest>,
    var order: MutableList<String>,
) : RollingFuelBurnRequestStore {
    override fun find(code: String): RollingFuelBurnRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnRequest) {
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

    override fun list(): MutableList<RollingFuelBurnRequest> {
        val out = mutableListOf<RollingFuelBurnRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnRequestStore(): RollingFuelBurnRequestStore {
    return MemoryRollingFuelBurnRequestStore(
        mutableMapOf<String, RollingFuelBurnRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnRequestStore(
    var inner: RollingFuelBurnRequestStore,
    var cache: TtlCache<RollingFuelBurnRequest>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnRequestStore {
    override fun find(code: String): RollingFuelBurnRequest? {
        val hit = (cache.get(code) as RollingFuelBurnRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnRequest> = inner.list()
}

fun newCachedRollingFuelBurnRequestStore(inner: RollingFuelBurnRequestStore, ttl: Int, clock: Clock): RollingFuelBurnRequestStore {
    return CachedRollingFuelBurnRequestStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn requests. Empty fields match all. */
data class RollingFuelBurnRequestQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFuelBurnRequest>): MutableList<RollingFuelBurnRequest> {
        val matched = filter(items) { candidate: RollingFuelBurnRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnRequests(store: RollingFuelBurnRequestStore, query: RollingFuelBurnRequestQuery): MutableList<RollingFuelBurnRequest> {
    return query.apply(store.list())
}
