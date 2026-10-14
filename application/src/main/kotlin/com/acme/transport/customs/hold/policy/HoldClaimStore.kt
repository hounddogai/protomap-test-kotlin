package com.acme.transport.customs.hold.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold claims by code. */
interface HoldClaimStore {
    fun find(code: String): HoldClaim?
    fun save(item: HoldClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldClaimStore(
    var items: MutableMap<String, HoldClaim>,
    var order: MutableList<String>,
) : HoldClaimStore {
    override fun find(code: String): HoldClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldClaim) {
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

    override fun list(): MutableList<HoldClaim> {
        val out = mutableListOf<HoldClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldClaimStore(): HoldClaimStore {
    return MemoryHoldClaimStore(mutableMapOf<String, HoldClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldClaimStore(
    var inner: HoldClaimStore,
    var cache: TtlCache<HoldClaim>,
    var hits: Int,
    var misses: Int,
) : HoldClaimStore {
    override fun find(code: String): HoldClaim? {
        val hit = (cache.get(code) as HoldClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldClaim> = inner.list()
}

fun newCachedHoldClaimStore(inner: HoldClaimStore, ttl: Int, clock: Clock): HoldClaimStore {
    return CachedHoldClaimStore(
        inner = inner,
        cache = newTtlCache<HoldClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold claims. Empty fields match all. */
data class HoldClaimQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldClaim>): MutableList<HoldClaim> {
        val matched = filter(items) { candidate: HoldClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldClaims(store: HoldClaimStore, query: HoldClaimQuery): MutableList<HoldClaim> {
    return query.apply(store.list())
}
