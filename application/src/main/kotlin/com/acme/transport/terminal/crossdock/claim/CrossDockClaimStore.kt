package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock claims by code. */
interface CrossDockClaimStore {
    fun find(code: String): CrossDockClaim?
    fun save(item: CrossDockClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockClaimStore(
    var items: MutableMap<String, CrossDockClaim>,
    var order: MutableList<String>,
) : CrossDockClaimStore {
    override fun find(code: String): CrossDockClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockClaim) {
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

    override fun list(): MutableList<CrossDockClaim> {
        val out = mutableListOf<CrossDockClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockClaimStore(): CrossDockClaimStore {
    return MemoryCrossDockClaimStore(mutableMapOf<String, CrossDockClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockClaimStore(
    var inner: CrossDockClaimStore,
    var cache: TtlCache<CrossDockClaim>,
    var hits: Int,
    var misses: Int,
) : CrossDockClaimStore {
    override fun find(code: String): CrossDockClaim? {
        val hit = (cache.get(code) as CrossDockClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockClaim> = inner.list()
}

fun newCachedCrossDockClaimStore(inner: CrossDockClaimStore, ttl: Int, clock: Clock): CrossDockClaimStore {
    return CachedCrossDockClaimStore(
        inner = inner,
        cache = newTtlCache<CrossDockClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock claims. Empty fields match all. */
data class CrossDockClaimQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockClaim>): MutableList<CrossDockClaim> {
        val matched = filter(items) { candidate: CrossDockClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockClaims(store: CrossDockClaimStore, query: CrossDockClaimQuery): MutableList<CrossDockClaim> {
    return query.apply(store.list())
}
