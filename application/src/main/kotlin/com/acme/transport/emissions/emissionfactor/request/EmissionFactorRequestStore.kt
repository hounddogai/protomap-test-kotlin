package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor requests by code. */
interface EmissionFactorRequestStore {
    fun find(code: String): EmissionFactorRequest?
    fun save(item: EmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorRequestStore(
    var items: MutableMap<String, EmissionFactorRequest>,
    var order: MutableList<String>,
) : EmissionFactorRequestStore {
    override fun find(code: String): EmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorRequest) {
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

    override fun list(): MutableList<EmissionFactorRequest> {
        val out = mutableListOf<EmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorRequestStore(): EmissionFactorRequestStore {
    return MemoryEmissionFactorRequestStore(
        mutableMapOf<String, EmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorRequestStore(
    var inner: EmissionFactorRequestStore,
    var cache: TtlCache<EmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorRequestStore {
    override fun find(code: String): EmissionFactorRequest? {
        val hit = (cache.get(code) as EmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorRequest> = inner.list()
}

fun newCachedEmissionFactorRequestStore(inner: EmissionFactorRequestStore, ttl: Int, clock: Clock): EmissionFactorRequestStore {
    return CachedEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor requests. Empty fields match all. */
data class EmissionFactorRequestQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<EmissionFactorRequest>): MutableList<EmissionFactorRequest> {
        val matched = filter(items) { candidate: EmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorRequests(store: EmissionFactorRequestStore, query: EmissionFactorRequestQuery): MutableList<EmissionFactorRequest> {
    return query.apply(store.list())
}
