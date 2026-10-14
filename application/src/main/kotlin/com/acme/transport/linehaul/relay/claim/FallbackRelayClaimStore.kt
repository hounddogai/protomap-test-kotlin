package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback relay claims by code. */
interface FallbackRelayClaimStore {
    fun find(code: String): FallbackRelayClaim?
    fun save(item: FallbackRelayClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRelayClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRelayClaimStore(
    var items: MutableMap<String, FallbackRelayClaim>,
    var order: MutableList<String>,
) : FallbackRelayClaimStore {
    override fun find(code: String): FallbackRelayClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRelayClaim) {
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

    override fun list(): MutableList<FallbackRelayClaim> {
        val out = mutableListOf<FallbackRelayClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRelayClaimStore(): FallbackRelayClaimStore {
    return MemoryFallbackRelayClaimStore(mutableMapOf<String, FallbackRelayClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRelayClaimStore(
    var inner: FallbackRelayClaimStore,
    var cache: TtlCache<FallbackRelayClaim>,
    var hits: Int,
    var misses: Int,
) : FallbackRelayClaimStore {
    override fun find(code: String): FallbackRelayClaim? {
        val hit = (cache.get(code) as FallbackRelayClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRelayClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRelayClaim> = inner.list()
}

fun newCachedFallbackRelayClaimStore(inner: FallbackRelayClaimStore, ttl: Int, clock: Clock): FallbackRelayClaimStore {
    return CachedFallbackRelayClaimStore(
        inner = inner,
        cache = newTtlCache<FallbackRelayClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback relay claims. Empty fields match all. */
data class FallbackRelayClaimQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRelayClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRelayClaim>): MutableList<FallbackRelayClaim> {
        val matched = filter(items) { candidate: FallbackRelayClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRelayClaims(store: FallbackRelayClaimStore, query: FallbackRelayClaimQuery): MutableList<FallbackRelayClaim> {
    return query.apply(store.list())
}
