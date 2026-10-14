package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped tender claims by code. */
interface CappedTenderClaimStore {
    fun find(code: String): CappedTenderClaim?
    fun save(item: CappedTenderClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTenderClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTenderClaimStore(
    var items: MutableMap<String, CappedTenderClaim>,
    var order: MutableList<String>,
) : CappedTenderClaimStore {
    override fun find(code: String): CappedTenderClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTenderClaim) {
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

    override fun list(): MutableList<CappedTenderClaim> {
        val out = mutableListOf<CappedTenderClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTenderClaimStore(): CappedTenderClaimStore {
    return MemoryCappedTenderClaimStore(mutableMapOf<String, CappedTenderClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTenderClaimStore(
    var inner: CappedTenderClaimStore,
    var cache: TtlCache<CappedTenderClaim>,
    var hits: Int,
    var misses: Int,
) : CappedTenderClaimStore {
    override fun find(code: String): CappedTenderClaim? {
        val hit = (cache.get(code) as CappedTenderClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTenderClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTenderClaim> = inner.list()
}

fun newCachedCappedTenderClaimStore(inner: CappedTenderClaimStore, ttl: Int, clock: Clock): CappedTenderClaimStore {
    return CachedCappedTenderClaimStore(
        inner = inner,
        cache = newTtlCache<CappedTenderClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tender claims. Empty fields match all. */
data class CappedTenderClaimQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTenderClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTenderClaim>): MutableList<CappedTenderClaim> {
        val matched = filter(items) { candidate: CappedTenderClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTenderClaims(store: CappedTenderClaimStore, query: CappedTenderClaimQuery): MutableList<CappedTenderClaim> {
    return query.apply(store.list())
}
