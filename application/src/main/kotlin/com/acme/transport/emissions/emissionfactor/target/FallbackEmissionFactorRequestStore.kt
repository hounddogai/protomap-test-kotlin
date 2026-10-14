package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor requests by code. */
interface FallbackEmissionFactorRequestStore {
    fun find(code: String): FallbackEmissionFactorRequest?
    fun save(item: FallbackEmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorRequestStore(
    var items: MutableMap<String, FallbackEmissionFactorRequest>,
    var order: MutableList<String>,
) : FallbackEmissionFactorRequestStore {
    override fun find(code: String): FallbackEmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorRequest) {
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

    override fun list(): MutableList<FallbackEmissionFactorRequest> {
        val out = mutableListOf<FallbackEmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorRequestStore(): FallbackEmissionFactorRequestStore {
    return MemoryFallbackEmissionFactorRequestStore(
        mutableMapOf<String, FallbackEmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorRequestStore(
    var inner: FallbackEmissionFactorRequestStore,
    var cache: TtlCache<FallbackEmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorRequestStore {
    override fun find(code: String): FallbackEmissionFactorRequest? {
        val hit = (cache.get(code) as FallbackEmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorRequest> = inner.list()
}

fun newCachedFallbackEmissionFactorRequestStore(inner: FallbackEmissionFactorRequestStore, ttl: Int, clock: Clock): FallbackEmissionFactorRequestStore {
    return CachedFallbackEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor requests. Empty fields match all. */
data class FallbackEmissionFactorRequestQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorRequest>): MutableList<FallbackEmissionFactorRequest> {
        val matched = filter(items) { candidate: FallbackEmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorRequests(store: FallbackEmissionFactorRequestStore, query: FallbackEmissionFactorRequestQuery): MutableList<FallbackEmissionFactorRequest> {
    return query.apply(store.list())
}
