package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional sortation claims by code. */
interface RegionalSortationClaimStore {
    fun find(code: String): RegionalSortationClaim?
    fun save(item: RegionalSortationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSortationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSortationClaimStore(
    var items: MutableMap<String, RegionalSortationClaim>,
    var order: MutableList<String>,
) : RegionalSortationClaimStore {
    override fun find(code: String): RegionalSortationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSortationClaim) {
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

    override fun list(): MutableList<RegionalSortationClaim> {
        val out = mutableListOf<RegionalSortationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSortationClaimStore(): RegionalSortationClaimStore {
    return MemoryRegionalSortationClaimStore(
        mutableMapOf<String, RegionalSortationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSortationClaimStore(
    var inner: RegionalSortationClaimStore,
    var cache: TtlCache<RegionalSortationClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalSortationClaimStore {
    override fun find(code: String): RegionalSortationClaim? {
        val hit = (cache.get(code) as RegionalSortationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSortationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSortationClaim> = inner.list()
}

fun newCachedRegionalSortationClaimStore(inner: RegionalSortationClaimStore, ttl: Int, clock: Clock): RegionalSortationClaimStore {
    return CachedRegionalSortationClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalSortationClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional sortation claims. Empty fields match all. */
data class RegionalSortationClaimQuery(
    var category: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSortationClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalSortationClaim>): MutableList<RegionalSortationClaim> {
        val matched = filter(items) { candidate: RegionalSortationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSortationClaims(store: RegionalSortationClaimStore, query: RegionalSortationClaimQuery): MutableList<RegionalSortationClaim> {
    return query.apply(store.list())
}
