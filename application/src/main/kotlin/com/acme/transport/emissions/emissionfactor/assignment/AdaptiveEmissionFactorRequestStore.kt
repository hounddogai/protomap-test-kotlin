package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor requests by code. */
interface AdaptiveEmissionFactorRequestStore {
    fun find(code: String): AdaptiveEmissionFactorRequest?
    fun save(item: AdaptiveEmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorRequestStore(
    var items: MutableMap<String, AdaptiveEmissionFactorRequest>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorRequestStore {
    override fun find(code: String): AdaptiveEmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorRequest) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorRequest> {
        val out = mutableListOf<AdaptiveEmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorRequestStore(): AdaptiveEmissionFactorRequestStore {
    return MemoryAdaptiveEmissionFactorRequestStore(
        mutableMapOf<String, AdaptiveEmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorRequestStore(
    var inner: AdaptiveEmissionFactorRequestStore,
    var cache: TtlCache<AdaptiveEmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorRequestStore {
    override fun find(code: String): AdaptiveEmissionFactorRequest? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorRequest> = inner.list()
}

fun newCachedAdaptiveEmissionFactorRequestStore(inner: AdaptiveEmissionFactorRequestStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorRequestStore {
    return CachedAdaptiveEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor requests. Empty fields match all. */
data class AdaptiveEmissionFactorRequestQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEmissionFactorRequest>): MutableList<AdaptiveEmissionFactorRequest> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorRequests(store: AdaptiveEmissionFactorRequestStore, query: AdaptiveEmissionFactorRequestQuery): MutableList<AdaptiveEmissionFactorRequest> {
    return query.apply(store.list())
}
