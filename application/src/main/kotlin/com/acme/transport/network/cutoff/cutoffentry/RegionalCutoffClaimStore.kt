package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional cutoff claims by code. */
interface RegionalCutoffClaimStore {
    fun find(code: String): RegionalCutoffClaim?
    fun save(item: RegionalCutoffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCutoffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCutoffClaimStore(
    var items: MutableMap<String, RegionalCutoffClaim>,
    var order: MutableList<String>,
) : RegionalCutoffClaimStore {
    override fun find(code: String): RegionalCutoffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCutoffClaim) {
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

    override fun list(): MutableList<RegionalCutoffClaim> {
        val out = mutableListOf<RegionalCutoffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCutoffClaimStore(): RegionalCutoffClaimStore {
    return MemoryRegionalCutoffClaimStore(mutableMapOf<String, RegionalCutoffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCutoffClaimStore(
    var inner: RegionalCutoffClaimStore,
    var cache: TtlCache<RegionalCutoffClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalCutoffClaimStore {
    override fun find(code: String): RegionalCutoffClaim? {
        val hit = (cache.get(code) as RegionalCutoffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCutoffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCutoffClaim> = inner.list()
}

fun newCachedRegionalCutoffClaimStore(inner: RegionalCutoffClaimStore, ttl: Int, clock: Clock): RegionalCutoffClaimStore {
    return CachedRegionalCutoffClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalCutoffClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cutoff claims. Empty fields match all. */
data class RegionalCutoffClaimQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCutoffClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCutoffClaim>): MutableList<RegionalCutoffClaim> {
        val matched = filter(items) { candidate: RegionalCutoffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCutoffClaims(store: RegionalCutoffClaimStore, query: RegionalCutoffClaimQuery): MutableList<RegionalCutoffClaim> {
    return query.apply(store.list())
}
