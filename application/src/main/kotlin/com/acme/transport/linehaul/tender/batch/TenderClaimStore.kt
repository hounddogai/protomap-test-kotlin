package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender claims by code. */
interface TenderClaimStore {
    fun find(code: String): TenderClaim?
    fun save(item: TenderClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderClaimStore(
    var items: MutableMap<String, TenderClaim>,
    var order: MutableList<String>,
) : TenderClaimStore {
    override fun find(code: String): TenderClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderClaim) {
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

    override fun list(): MutableList<TenderClaim> {
        val out = mutableListOf<TenderClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderClaimStore(): TenderClaimStore {
    return MemoryTenderClaimStore(mutableMapOf<String, TenderClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderClaimStore(
    var inner: TenderClaimStore,
    var cache: TtlCache<TenderClaim>,
    var hits: Int,
    var misses: Int,
) : TenderClaimStore {
    override fun find(code: String): TenderClaim? {
        val hit = (cache.get(code) as TenderClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderClaim> = inner.list()
}

fun newCachedTenderClaimStore(inner: TenderClaimStore, ttl: Int, clock: Clock): TenderClaimStore {
    return CachedTenderClaimStore(
        inner = inner,
        cache = newTtlCache<TenderClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender claims. Empty fields match all. */
data class TenderClaimQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderClaim>): MutableList<TenderClaim> {
        val matched = filter(items) { candidate: TenderClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderClaims(store: TenderClaimStore, query: TenderClaimQuery): MutableList<TenderClaim> {
    return query.apply(store.list())
}
