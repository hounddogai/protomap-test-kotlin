package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive relay requests by code. */
interface AdaptiveRelayRequestStore {
    fun find(code: String): AdaptiveRelayRequest?
    fun save(item: AdaptiveRelayRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRelayRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRelayRequestStore(
    var items: MutableMap<String, AdaptiveRelayRequest>,
    var order: MutableList<String>,
) : AdaptiveRelayRequestStore {
    override fun find(code: String): AdaptiveRelayRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRelayRequest) {
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

    override fun list(): MutableList<AdaptiveRelayRequest> {
        val out = mutableListOf<AdaptiveRelayRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRelayRequestStore(): AdaptiveRelayRequestStore {
    return MemoryAdaptiveRelayRequestStore(
        mutableMapOf<String, AdaptiveRelayRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRelayRequestStore(
    var inner: AdaptiveRelayRequestStore,
    var cache: TtlCache<AdaptiveRelayRequest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRelayRequestStore {
    override fun find(code: String): AdaptiveRelayRequest? {
        val hit = (cache.get(code) as AdaptiveRelayRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRelayRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRelayRequest> = inner.list()
}

fun newCachedAdaptiveRelayRequestStore(inner: AdaptiveRelayRequestStore, ttl: Int, clock: Clock): AdaptiveRelayRequestStore {
    return CachedAdaptiveRelayRequestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRelayRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive relay requests. Empty fields match all. */
data class AdaptiveRelayRequestQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRelayRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRelayRequest>): MutableList<AdaptiveRelayRequest> {
        val matched = filter(items) { candidate: AdaptiveRelayRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRelayRequests(store: AdaptiveRelayRequestStore, query: AdaptiveRelayRequestQuery): MutableList<AdaptiveRelayRequest> {
    return query.apply(store.list())
}
