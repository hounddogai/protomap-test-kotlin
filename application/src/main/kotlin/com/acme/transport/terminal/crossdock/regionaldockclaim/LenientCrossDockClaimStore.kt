package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient cross dock claims by code. */
interface LenientCrossDockClaimStore {
    fun find(code: String): LenientCrossDockClaim?
    fun save(item: LenientCrossDockClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCrossDockClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCrossDockClaimStore(
    var items: MutableMap<String, LenientCrossDockClaim>,
    var order: MutableList<String>,
) : LenientCrossDockClaimStore {
    override fun find(code: String): LenientCrossDockClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCrossDockClaim) {
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

    override fun list(): MutableList<LenientCrossDockClaim> {
        val out = mutableListOf<LenientCrossDockClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCrossDockClaimStore(): LenientCrossDockClaimStore {
    return MemoryLenientCrossDockClaimStore(
        mutableMapOf<String, LenientCrossDockClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCrossDockClaimStore(
    var inner: LenientCrossDockClaimStore,
    var cache: TtlCache<LenientCrossDockClaim>,
    var hits: Int,
    var misses: Int,
) : LenientCrossDockClaimStore {
    override fun find(code: String): LenientCrossDockClaim? {
        val hit = (cache.get(code) as LenientCrossDockClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCrossDockClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCrossDockClaim> = inner.list()
}

fun newCachedLenientCrossDockClaimStore(inner: LenientCrossDockClaimStore, ttl: Int, clock: Clock): LenientCrossDockClaimStore {
    return CachedLenientCrossDockClaimStore(
        inner = inner,
        cache = newTtlCache<LenientCrossDockClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cross dock claims. Empty fields match all. */
data class LenientCrossDockClaimQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCrossDockClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCrossDockClaim>): MutableList<LenientCrossDockClaim> {
        val matched = filter(items) { candidate: LenientCrossDockClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCrossDockClaims(store: LenientCrossDockClaimStore, query: LenientCrossDockClaimQuery): MutableList<LenientCrossDockClaim> {
    return query.apply(store.list())
}
