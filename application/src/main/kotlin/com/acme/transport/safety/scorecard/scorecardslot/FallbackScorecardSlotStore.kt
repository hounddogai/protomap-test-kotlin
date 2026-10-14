package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback scorecard slots by code. */
interface FallbackScorecardSlotStore {
    fun find(code: String): FallbackScorecardSlot?
    fun save(item: FallbackScorecardSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackScorecardSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackScorecardSlotStore(
    var items: MutableMap<String, FallbackScorecardSlot>,
    var order: MutableList<String>,
) : FallbackScorecardSlotStore {
    override fun find(code: String): FallbackScorecardSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackScorecardSlot) {
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

    override fun list(): MutableList<FallbackScorecardSlot> {
        val out = mutableListOf<FallbackScorecardSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackScorecardSlotStore(): FallbackScorecardSlotStore {
    return MemoryFallbackScorecardSlotStore(
        mutableMapOf<String, FallbackScorecardSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackScorecardSlotStore(
    var inner: FallbackScorecardSlotStore,
    var cache: TtlCache<FallbackScorecardSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackScorecardSlotStore {
    override fun find(code: String): FallbackScorecardSlot? {
        val hit = (cache.get(code) as FallbackScorecardSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackScorecardSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackScorecardSlot> = inner.list()
}

fun newCachedFallbackScorecardSlotStore(inner: FallbackScorecardSlotStore, ttl: Int, clock: Clock): FallbackScorecardSlotStore {
    return CachedFallbackScorecardSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackScorecardSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback scorecard slots. Empty fields match all. */
data class FallbackScorecardSlotQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackScorecardSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackScorecardSlot>): MutableList<FallbackScorecardSlot> {
        val matched = filter(items) { candidate: FallbackScorecardSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackScorecardSlots(store: FallbackScorecardSlotStore, query: FallbackScorecardSlotQuery): MutableList<FallbackScorecardSlot> {
    return query.apply(store.list())
}
