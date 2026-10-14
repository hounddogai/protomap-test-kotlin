package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled trailer yard claims by code. */
interface ScheduledTrailerYardClaimStore {
    fun find(code: String): ScheduledTrailerYardClaim?
    fun save(item: ScheduledTrailerYardClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerYardClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerYardClaimStore(
    var items: MutableMap<String, ScheduledTrailerYardClaim>,
    var order: MutableList<String>,
) : ScheduledTrailerYardClaimStore {
    override fun find(code: String): ScheduledTrailerYardClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerYardClaim) {
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

    override fun list(): MutableList<ScheduledTrailerYardClaim> {
        val out = mutableListOf<ScheduledTrailerYardClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerYardClaimStore(): ScheduledTrailerYardClaimStore {
    return MemoryScheduledTrailerYardClaimStore(
        mutableMapOf<String, ScheduledTrailerYardClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerYardClaimStore(
    var inner: ScheduledTrailerYardClaimStore,
    var cache: TtlCache<ScheduledTrailerYardClaim>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerYardClaimStore {
    override fun find(code: String): ScheduledTrailerYardClaim? {
        val hit = (cache.get(code) as ScheduledTrailerYardClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerYardClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerYardClaim> = inner.list()
}

fun newCachedScheduledTrailerYardClaimStore(inner: ScheduledTrailerYardClaimStore, ttl: Int, clock: Clock): ScheduledTrailerYardClaimStore {
    return CachedScheduledTrailerYardClaimStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerYardClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer yard claims. Empty fields match all. */
data class ScheduledTrailerYardClaimQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerYardClaim): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerYardClaim>): MutableList<ScheduledTrailerYardClaim> {
        val matched = filter(items) { candidate: ScheduledTrailerYardClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerYardClaims(store: ScheduledTrailerYardClaimStore, query: ScheduledTrailerYardClaimQuery): MutableList<ScheduledTrailerYardClaim> {
    return query.apply(store.list())
}
