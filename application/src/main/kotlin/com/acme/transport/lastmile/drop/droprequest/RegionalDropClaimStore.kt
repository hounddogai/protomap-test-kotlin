package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional drop claims by code. */
interface RegionalDropClaimStore {
    fun find(code: String): RegionalDropClaim?
    fun save(item: RegionalDropClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDropClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDropClaimStore(
    var items: MutableMap<String, RegionalDropClaim>,
    var order: MutableList<String>,
) : RegionalDropClaimStore {
    override fun find(code: String): RegionalDropClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDropClaim) {
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

    override fun list(): MutableList<RegionalDropClaim> {
        val out = mutableListOf<RegionalDropClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDropClaimStore(): RegionalDropClaimStore {
    return MemoryRegionalDropClaimStore(mutableMapOf<String, RegionalDropClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDropClaimStore(
    var inner: RegionalDropClaimStore,
    var cache: TtlCache<RegionalDropClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalDropClaimStore {
    override fun find(code: String): RegionalDropClaim? {
        val hit = (cache.get(code) as RegionalDropClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDropClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDropClaim> = inner.list()
}

fun newCachedRegionalDropClaimStore(inner: RegionalDropClaimStore, ttl: Int, clock: Clock): RegionalDropClaimStore {
    return CachedRegionalDropClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalDropClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional drop claims. Empty fields match all. */
data class RegionalDropClaimQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDropClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDropClaim>): MutableList<RegionalDropClaim> {
        val matched = filter(items) { candidate: RegionalDropClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDropClaims(store: RegionalDropClaimStore, query: RegionalDropClaimQuery): MutableList<RegionalDropClaim> {
    return query.apply(store.list())
}
