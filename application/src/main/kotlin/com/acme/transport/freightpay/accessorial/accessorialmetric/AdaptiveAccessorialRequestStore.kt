package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive accessorial requests by code. */
interface AdaptiveAccessorialRequestStore {
    fun find(code: String): AdaptiveAccessorialRequest?
    fun save(item: AdaptiveAccessorialRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAccessorialRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAccessorialRequestStore(
    var items: MutableMap<String, AdaptiveAccessorialRequest>,
    var order: MutableList<String>,
) : AdaptiveAccessorialRequestStore {
    override fun find(code: String): AdaptiveAccessorialRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAccessorialRequest) {
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

    override fun list(): MutableList<AdaptiveAccessorialRequest> {
        val out = mutableListOf<AdaptiveAccessorialRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAccessorialRequestStore(): AdaptiveAccessorialRequestStore {
    return MemoryAdaptiveAccessorialRequestStore(
        mutableMapOf<String, AdaptiveAccessorialRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAccessorialRequestStore(
    var inner: AdaptiveAccessorialRequestStore,
    var cache: TtlCache<AdaptiveAccessorialRequest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAccessorialRequestStore {
    override fun find(code: String): AdaptiveAccessorialRequest? {
        val hit = (cache.get(code) as AdaptiveAccessorialRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAccessorialRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAccessorialRequest> = inner.list()
}

fun newCachedAdaptiveAccessorialRequestStore(inner: AdaptiveAccessorialRequestStore, ttl: Int, clock: Clock): AdaptiveAccessorialRequestStore {
    return CachedAdaptiveAccessorialRequestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAccessorialRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive accessorial requests. Empty fields match all. */
data class AdaptiveAccessorialRequestQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAccessorialRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAccessorialRequest>): MutableList<AdaptiveAccessorialRequest> {
        val matched = filter(items) { candidate: AdaptiveAccessorialRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAccessorialRequests(store: AdaptiveAccessorialRequestStore, query: AdaptiveAccessorialRequestQuery): MutableList<AdaptiveAccessorialRequest> {
    return query.apply(store.list())
}
