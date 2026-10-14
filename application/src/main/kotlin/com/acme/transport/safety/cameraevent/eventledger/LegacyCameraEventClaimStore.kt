package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy camera event claims by code. */
interface LegacyCameraEventClaimStore {
    fun find(code: String): LegacyCameraEventClaim?
    fun save(item: LegacyCameraEventClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCameraEventClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCameraEventClaimStore(
    var items: MutableMap<String, LegacyCameraEventClaim>,
    var order: MutableList<String>,
) : LegacyCameraEventClaimStore {
    override fun find(code: String): LegacyCameraEventClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCameraEventClaim) {
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

    override fun list(): MutableList<LegacyCameraEventClaim> {
        val out = mutableListOf<LegacyCameraEventClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCameraEventClaimStore(): LegacyCameraEventClaimStore {
    return MemoryLegacyCameraEventClaimStore(
        mutableMapOf<String, LegacyCameraEventClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCameraEventClaimStore(
    var inner: LegacyCameraEventClaimStore,
    var cache: TtlCache<LegacyCameraEventClaim>,
    var hits: Int,
    var misses: Int,
) : LegacyCameraEventClaimStore {
    override fun find(code: String): LegacyCameraEventClaim? {
        val hit = (cache.get(code) as LegacyCameraEventClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCameraEventClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCameraEventClaim> = inner.list()
}

fun newCachedLegacyCameraEventClaimStore(inner: LegacyCameraEventClaimStore, ttl: Int, clock: Clock): LegacyCameraEventClaimStore {
    return CachedLegacyCameraEventClaimStore(
        inner = inner,
        cache = newTtlCache<LegacyCameraEventClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy camera event claims. Empty fields match all. */
data class LegacyCameraEventClaimQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCameraEventClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LegacyCameraEventClaim>): MutableList<LegacyCameraEventClaim> {
        val matched = filter(items) { candidate: LegacyCameraEventClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCameraEventClaims(store: LegacyCameraEventClaimStore, query: LegacyCameraEventClaimQuery): MutableList<LegacyCameraEventClaim> {
    return query.apply(store.list())
}
