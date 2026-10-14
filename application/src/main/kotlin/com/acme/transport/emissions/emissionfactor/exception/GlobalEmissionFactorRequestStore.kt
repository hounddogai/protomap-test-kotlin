package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global emission factor requests by code. */
interface GlobalEmissionFactorRequestStore {
    fun find(code: String): GlobalEmissionFactorRequest?
    fun save(item: GlobalEmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalEmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalEmissionFactorRequestStore(
    var items: MutableMap<String, GlobalEmissionFactorRequest>,
    var order: MutableList<String>,
) : GlobalEmissionFactorRequestStore {
    override fun find(code: String): GlobalEmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalEmissionFactorRequest) {
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

    override fun list(): MutableList<GlobalEmissionFactorRequest> {
        val out = mutableListOf<GlobalEmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalEmissionFactorRequestStore(): GlobalEmissionFactorRequestStore {
    return MemoryGlobalEmissionFactorRequestStore(
        mutableMapOf<String, GlobalEmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalEmissionFactorRequestStore(
    var inner: GlobalEmissionFactorRequestStore,
    var cache: TtlCache<GlobalEmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : GlobalEmissionFactorRequestStore {
    override fun find(code: String): GlobalEmissionFactorRequest? {
        val hit = (cache.get(code) as GlobalEmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalEmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalEmissionFactorRequest> = inner.list()
}

fun newCachedGlobalEmissionFactorRequestStore(inner: GlobalEmissionFactorRequestStore, ttl: Int, clock: Clock): GlobalEmissionFactorRequestStore {
    return CachedGlobalEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<GlobalEmissionFactorRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global emission factor requests. Empty fields match all. */
data class GlobalEmissionFactorRequestQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalEmissionFactorRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalEmissionFactorRequest>): MutableList<GlobalEmissionFactorRequest> {
        val matched = filter(items) { candidate: GlobalEmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalEmissionFactorRequests(store: GlobalEmissionFactorRequestStore, query: GlobalEmissionFactorRequestQuery): MutableList<GlobalEmissionFactorRequest> {
    return query.apply(store.list())
}
