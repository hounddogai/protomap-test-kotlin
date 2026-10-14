package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled cross dock claims by code. */
interface ScheduledCrossDockClaimStore {
    fun find(code: String): ScheduledCrossDockClaim?
    fun save(item: ScheduledCrossDockClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCrossDockClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCrossDockClaimStore(
    var items: MutableMap<String, ScheduledCrossDockClaim>,
    var order: MutableList<String>,
) : ScheduledCrossDockClaimStore {
    override fun find(code: String): ScheduledCrossDockClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCrossDockClaim) {
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

    override fun list(): MutableList<ScheduledCrossDockClaim> {
        val out = mutableListOf<ScheduledCrossDockClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCrossDockClaimStore(): ScheduledCrossDockClaimStore {
    return MemoryScheduledCrossDockClaimStore(
        mutableMapOf<String, ScheduledCrossDockClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCrossDockClaimStore(
    var inner: ScheduledCrossDockClaimStore,
    var cache: TtlCache<ScheduledCrossDockClaim>,
    var hits: Int,
    var misses: Int,
) : ScheduledCrossDockClaimStore {
    override fun find(code: String): ScheduledCrossDockClaim? {
        val hit = (cache.get(code) as ScheduledCrossDockClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCrossDockClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCrossDockClaim> = inner.list()
}

fun newCachedScheduledCrossDockClaimStore(inner: ScheduledCrossDockClaimStore, ttl: Int, clock: Clock): ScheduledCrossDockClaimStore {
    return CachedScheduledCrossDockClaimStore(
        inner = inner,
        cache = newTtlCache<ScheduledCrossDockClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled cross dock claims. Empty fields match all. */
data class ScheduledCrossDockClaimQuery(
    var segment: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCrossDockClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCrossDockClaim>): MutableList<ScheduledCrossDockClaim> {
        val matched = filter(items) { candidate: ScheduledCrossDockClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCrossDockClaims(store: ScheduledCrossDockClaimStore, query: ScheduledCrossDockClaimQuery): MutableList<ScheduledCrossDockClaim> {
    return query.apply(store.list())
}
