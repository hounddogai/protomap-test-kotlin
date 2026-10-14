package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay claims by code. */
interface RelayClaimStore {
    fun find(code: String): RelayClaim?
    fun save(item: RelayClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayClaimStore(
    var items: MutableMap<String, RelayClaim>,
    var order: MutableList<String>,
) : RelayClaimStore {
    override fun find(code: String): RelayClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayClaim) {
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

    override fun list(): MutableList<RelayClaim> {
        val out = mutableListOf<RelayClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayClaimStore(): RelayClaimStore {
    return MemoryRelayClaimStore(mutableMapOf<String, RelayClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayClaimStore(
    var inner: RelayClaimStore,
    var cache: TtlCache<RelayClaim>,
    var hits: Int,
    var misses: Int,
) : RelayClaimStore {
    override fun find(code: String): RelayClaim? {
        val hit = (cache.get(code) as RelayClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayClaim> = inner.list()
}

fun newCachedRelayClaimStore(inner: RelayClaimStore, ttl: Int, clock: Clock): RelayClaimStore {
    return CachedRelayClaimStore(
        inner = inner,
        cache = newTtlCache<RelayClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay claims. Empty fields match all. */
data class RelayClaimQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayClaim>): MutableList<RelayClaim> {
        val matched = filter(items) { candidate: RelayClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayClaims(store: RelayClaimStore, query: RelayClaimQuery): MutableList<RelayClaim> {
    return query.apply(store.list())
}
