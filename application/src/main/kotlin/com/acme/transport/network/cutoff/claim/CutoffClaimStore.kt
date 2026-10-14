package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff claims by code. */
interface CutoffClaimStore {
    fun find(code: String): CutoffClaim?
    fun save(item: CutoffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffClaimStore(
    var items: MutableMap<String, CutoffClaim>,
    var order: MutableList<String>,
) : CutoffClaimStore {
    override fun find(code: String): CutoffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffClaim) {
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

    override fun list(): MutableList<CutoffClaim> {
        val out = mutableListOf<CutoffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffClaimStore(): CutoffClaimStore {
    return MemoryCutoffClaimStore(mutableMapOf<String, CutoffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffClaimStore(
    var inner: CutoffClaimStore,
    var cache: TtlCache<CutoffClaim>,
    var hits: Int,
    var misses: Int,
) : CutoffClaimStore {
    override fun find(code: String): CutoffClaim? {
        val hit = (cache.get(code) as CutoffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffClaim> = inner.list()
}

fun newCachedCutoffClaimStore(inner: CutoffClaimStore, ttl: Int, clock: Clock): CutoffClaimStore {
    return CachedCutoffClaimStore(
        inner = inner,
        cache = newTtlCache<CutoffClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff claims. Empty fields match all. */
data class CutoffClaimQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CutoffClaim>): MutableList<CutoffClaim> {
        val matched = filter(items) { candidate: CutoffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffClaims(store: CutoffClaimStore, query: CutoffClaimQuery): MutableList<CutoffClaim> {
    return query.apply(store.list())
}
