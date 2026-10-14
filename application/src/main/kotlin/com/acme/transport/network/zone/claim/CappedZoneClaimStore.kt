package com.acme.transport.network.zone.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped zone claims by code. */
interface CappedZoneClaimStore {
    fun find(code: String): CappedZoneClaim?
    fun save(item: CappedZoneClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedZoneClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedZoneClaimStore(
    var items: MutableMap<String, CappedZoneClaim>,
    var order: MutableList<String>,
) : CappedZoneClaimStore {
    override fun find(code: String): CappedZoneClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedZoneClaim) {
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

    override fun list(): MutableList<CappedZoneClaim> {
        val out = mutableListOf<CappedZoneClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedZoneClaimStore(): CappedZoneClaimStore {
    return MemoryCappedZoneClaimStore(mutableMapOf<String, CappedZoneClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedZoneClaimStore(
    var inner: CappedZoneClaimStore,
    var cache: TtlCache<CappedZoneClaim>,
    var hits: Int,
    var misses: Int,
) : CappedZoneClaimStore {
    override fun find(code: String): CappedZoneClaim? {
        val hit = (cache.get(code) as CappedZoneClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedZoneClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedZoneClaim> = inner.list()
}

fun newCachedCappedZoneClaimStore(inner: CappedZoneClaimStore, ttl: Int, clock: Clock): CappedZoneClaimStore {
    return CachedCappedZoneClaimStore(
        inner = inner,
        cache = newTtlCache<CappedZoneClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped zone claims. Empty fields match all. */
data class CappedZoneClaimQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedZoneClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedZoneClaim>): MutableList<CappedZoneClaim> {
        val matched = filter(items) { candidate: CappedZoneClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedZoneClaims(store: CappedZoneClaimStore, query: CappedZoneClaimQuery): MutableList<CappedZoneClaim> {
    return query.apply(store.list())
}
