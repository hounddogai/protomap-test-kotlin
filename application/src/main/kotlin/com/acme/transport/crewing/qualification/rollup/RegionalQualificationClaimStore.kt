package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional qualification claims by code. */
interface RegionalQualificationClaimStore {
    fun find(code: String): RegionalQualificationClaim?
    fun save(item: RegionalQualificationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalQualificationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalQualificationClaimStore(
    var items: MutableMap<String, RegionalQualificationClaim>,
    var order: MutableList<String>,
) : RegionalQualificationClaimStore {
    override fun find(code: String): RegionalQualificationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalQualificationClaim) {
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

    override fun list(): MutableList<RegionalQualificationClaim> {
        val out = mutableListOf<RegionalQualificationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalQualificationClaimStore(): RegionalQualificationClaimStore {
    return MemoryRegionalQualificationClaimStore(
        mutableMapOf<String, RegionalQualificationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalQualificationClaimStore(
    var inner: RegionalQualificationClaimStore,
    var cache: TtlCache<RegionalQualificationClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalQualificationClaimStore {
    override fun find(code: String): RegionalQualificationClaim? {
        val hit = (cache.get(code) as RegionalQualificationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalQualificationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalQualificationClaim> = inner.list()
}

fun newCachedRegionalQualificationClaimStore(inner: RegionalQualificationClaimStore, ttl: Int, clock: Clock): RegionalQualificationClaimStore {
    return CachedRegionalQualificationClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalQualificationClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional qualification claims. Empty fields match all. */
data class RegionalQualificationClaimQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalQualificationClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalQualificationClaim>): MutableList<RegionalQualificationClaim> {
        val matched = filter(items) { candidate: RegionalQualificationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalQualificationClaims(store: RegionalQualificationClaimStore, query: RegionalQualificationClaimQuery): MutableList<RegionalQualificationClaim> {
    return query.apply(store.list())
}
