package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul claims by code. */
interface BackhaulClaimStore {
    fun find(code: String): BackhaulClaim?
    fun save(item: BackhaulClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulClaimStore(
    var items: MutableMap<String, BackhaulClaim>,
    var order: MutableList<String>,
) : BackhaulClaimStore {
    override fun find(code: String): BackhaulClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulClaim) {
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

    override fun list(): MutableList<BackhaulClaim> {
        val out = mutableListOf<BackhaulClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulClaimStore(): BackhaulClaimStore {
    return MemoryBackhaulClaimStore(mutableMapOf<String, BackhaulClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulClaimStore(
    var inner: BackhaulClaimStore,
    var cache: TtlCache<BackhaulClaim>,
    var hits: Int,
    var misses: Int,
) : BackhaulClaimStore {
    override fun find(code: String): BackhaulClaim? {
        val hit = (cache.get(code) as BackhaulClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulClaim> = inner.list()
}

fun newCachedBackhaulClaimStore(inner: BackhaulClaimStore, ttl: Int, clock: Clock): BackhaulClaimStore {
    return CachedBackhaulClaimStore(
        inner = inner,
        cache = newTtlCache<BackhaulClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul claims. Empty fields match all. */
data class BackhaulClaimQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulClaim>): MutableList<BackhaulClaim> {
        val matched = filter(items) { candidate: BackhaulClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulClaims(store: BackhaulClaimStore, query: BackhaulClaimQuery): MutableList<BackhaulClaim> {
    return query.apply(store.list())
}
