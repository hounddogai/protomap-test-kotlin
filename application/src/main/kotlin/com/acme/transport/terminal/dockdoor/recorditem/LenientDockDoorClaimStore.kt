package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dock door claims by code. */
interface LenientDockDoorClaimStore {
    fun find(code: String): LenientDockDoorClaim?
    fun save(item: LenientDockDoorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDockDoorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDockDoorClaimStore(
    var items: MutableMap<String, LenientDockDoorClaim>,
    var order: MutableList<String>,
) : LenientDockDoorClaimStore {
    override fun find(code: String): LenientDockDoorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDockDoorClaim) {
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

    override fun list(): MutableList<LenientDockDoorClaim> {
        val out = mutableListOf<LenientDockDoorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDockDoorClaimStore(): LenientDockDoorClaimStore {
    return MemoryLenientDockDoorClaimStore(
        mutableMapOf<String, LenientDockDoorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDockDoorClaimStore(
    var inner: LenientDockDoorClaimStore,
    var cache: TtlCache<LenientDockDoorClaim>,
    var hits: Int,
    var misses: Int,
) : LenientDockDoorClaimStore {
    override fun find(code: String): LenientDockDoorClaim? {
        val hit = (cache.get(code) as LenientDockDoorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDockDoorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDockDoorClaim> = inner.list()
}

fun newCachedLenientDockDoorClaimStore(inner: LenientDockDoorClaimStore, ttl: Int, clock: Clock): LenientDockDoorClaimStore {
    return CachedLenientDockDoorClaimStore(
        inner = inner,
        cache = newTtlCache<LenientDockDoorClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dock door claims. Empty fields match all. */
data class LenientDockDoorClaimQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDockDoorClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDockDoorClaim>): MutableList<LenientDockDoorClaim> {
        val matched = filter(items) { candidate: LenientDockDoorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDockDoorClaims(store: LenientDockDoorClaimStore, query: LenientDockDoorClaimQuery): MutableList<LenientDockDoorClaim> {
    return query.apply(store.list())
}
