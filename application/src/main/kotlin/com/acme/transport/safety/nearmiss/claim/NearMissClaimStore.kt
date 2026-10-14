package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss claims by code. */
interface NearMissClaimStore {
    fun find(code: String): NearMissClaim?
    fun save(item: NearMissClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissClaimStore(
    var items: MutableMap<String, NearMissClaim>,
    var order: MutableList<String>,
) : NearMissClaimStore {
    override fun find(code: String): NearMissClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissClaim) {
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

    override fun list(): MutableList<NearMissClaim> {
        val out = mutableListOf<NearMissClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissClaimStore(): NearMissClaimStore {
    return MemoryNearMissClaimStore(mutableMapOf<String, NearMissClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissClaimStore(
    var inner: NearMissClaimStore,
    var cache: TtlCache<NearMissClaim>,
    var hits: Int,
    var misses: Int,
) : NearMissClaimStore {
    override fun find(code: String): NearMissClaim? {
        val hit = (cache.get(code) as NearMissClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissClaim> = inner.list()
}

fun newCachedNearMissClaimStore(inner: NearMissClaimStore, ttl: Int, clock: Clock): NearMissClaimStore {
    return CachedNearMissClaimStore(
        inner = inner,
        cache = newTtlCache<NearMissClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss claims. Empty fields match all. */
data class NearMissClaimQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissClaim>): MutableList<NearMissClaim> {
        val matched = filter(items) { candidate: NearMissClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissClaims(store: NearMissClaimStore, query: NearMissClaimQuery): MutableList<NearMissClaim> {
    return query.apply(store.list())
}
