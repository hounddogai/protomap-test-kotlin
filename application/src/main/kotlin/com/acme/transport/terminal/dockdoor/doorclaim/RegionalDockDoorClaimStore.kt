package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional dock door claims by code. */
interface RegionalDockDoorClaimStore {
    fun find(code: String): RegionalDockDoorClaim?
    fun save(item: RegionalDockDoorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDockDoorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDockDoorClaimStore(
    var items: MutableMap<String, RegionalDockDoorClaim>,
    var order: MutableList<String>,
) : RegionalDockDoorClaimStore {
    override fun find(code: String): RegionalDockDoorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDockDoorClaim) {
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

    override fun list(): MutableList<RegionalDockDoorClaim> {
        val out = mutableListOf<RegionalDockDoorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDockDoorClaimStore(): RegionalDockDoorClaimStore {
    return MemoryRegionalDockDoorClaimStore(
        mutableMapOf<String, RegionalDockDoorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDockDoorClaimStore(
    var inner: RegionalDockDoorClaimStore,
    var cache: TtlCache<RegionalDockDoorClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalDockDoorClaimStore {
    override fun find(code: String): RegionalDockDoorClaim? {
        val hit = (cache.get(code) as RegionalDockDoorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDockDoorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDockDoorClaim> = inner.list()
}

fun newCachedRegionalDockDoorClaimStore(inner: RegionalDockDoorClaimStore, ttl: Int, clock: Clock): RegionalDockDoorClaimStore {
    return CachedRegionalDockDoorClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalDockDoorClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dock door claims. Empty fields match all. */
data class RegionalDockDoorClaimQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDockDoorClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RegionalDockDoorClaim>): MutableList<RegionalDockDoorClaim> {
        val matched = filter(items) { candidate: RegionalDockDoorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDockDoorClaims(store: RegionalDockDoorClaimStore, query: RegionalDockDoorClaimQuery): MutableList<RegionalDockDoorClaim> {
    return query.apply(store.list())
}
