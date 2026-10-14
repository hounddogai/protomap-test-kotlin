package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy trailer yard claims by code. */
interface LegacyTrailerYardClaimStore {
    fun find(code: String): LegacyTrailerYardClaim?
    fun save(item: LegacyTrailerYardClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerYardClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerYardClaimStore(
    var items: MutableMap<String, LegacyTrailerYardClaim>,
    var order: MutableList<String>,
) : LegacyTrailerYardClaimStore {
    override fun find(code: String): LegacyTrailerYardClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerYardClaim) {
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

    override fun list(): MutableList<LegacyTrailerYardClaim> {
        val out = mutableListOf<LegacyTrailerYardClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerYardClaimStore(): LegacyTrailerYardClaimStore {
    return MemoryLegacyTrailerYardClaimStore(
        mutableMapOf<String, LegacyTrailerYardClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerYardClaimStore(
    var inner: LegacyTrailerYardClaimStore,
    var cache: TtlCache<LegacyTrailerYardClaim>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerYardClaimStore {
    override fun find(code: String): LegacyTrailerYardClaim? {
        val hit = (cache.get(code) as LegacyTrailerYardClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerYardClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerYardClaim> = inner.list()
}

fun newCachedLegacyTrailerYardClaimStore(inner: LegacyTrailerYardClaimStore, ttl: Int, clock: Clock): LegacyTrailerYardClaimStore {
    return CachedLegacyTrailerYardClaimStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerYardClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer yard claims. Empty fields match all. */
data class LegacyTrailerYardClaimQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerYardClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerYardClaim>): MutableList<LegacyTrailerYardClaim> {
        val matched = filter(items) { candidate: LegacyTrailerYardClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerYardClaims(store: LegacyTrailerYardClaimStore, query: LegacyTrailerYardClaimQuery): MutableList<LegacyTrailerYardClaim> {
    return query.apply(store.list())
}
