package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient emission factor requests by code. */
interface LenientEmissionFactorRequestStore {
    fun find(code: String): LenientEmissionFactorRequest?
    fun save(item: LenientEmissionFactorRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientEmissionFactorRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientEmissionFactorRequestStore(
    var items: MutableMap<String, LenientEmissionFactorRequest>,
    var order: MutableList<String>,
) : LenientEmissionFactorRequestStore {
    override fun find(code: String): LenientEmissionFactorRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientEmissionFactorRequest) {
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

    override fun list(): MutableList<LenientEmissionFactorRequest> {
        val out = mutableListOf<LenientEmissionFactorRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientEmissionFactorRequestStore(): LenientEmissionFactorRequestStore {
    return MemoryLenientEmissionFactorRequestStore(
        mutableMapOf<String, LenientEmissionFactorRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientEmissionFactorRequestStore(
    var inner: LenientEmissionFactorRequestStore,
    var cache: TtlCache<LenientEmissionFactorRequest>,
    var hits: Int,
    var misses: Int,
) : LenientEmissionFactorRequestStore {
    override fun find(code: String): LenientEmissionFactorRequest? {
        val hit = (cache.get(code) as LenientEmissionFactorRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientEmissionFactorRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientEmissionFactorRequest> = inner.list()
}

fun newCachedLenientEmissionFactorRequestStore(inner: LenientEmissionFactorRequestStore, ttl: Int, clock: Clock): LenientEmissionFactorRequestStore {
    return CachedLenientEmissionFactorRequestStore(
        inner = inner,
        cache = newTtlCache<LenientEmissionFactorRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient emission factor requests. Empty fields match all. */
data class LenientEmissionFactorRequestQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientEmissionFactorRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientEmissionFactorRequest>): MutableList<LenientEmissionFactorRequest> {
        val matched = filter(items) { candidate: LenientEmissionFactorRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientEmissionFactorRequests(store: LenientEmissionFactorRequestStore, query: LenientEmissionFactorRequestQuery): MutableList<LenientEmissionFactorRequest> {
    return query.apply(store.list())
}
