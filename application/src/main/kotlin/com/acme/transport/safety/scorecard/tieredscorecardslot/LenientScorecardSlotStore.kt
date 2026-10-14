package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient scorecard slots by code. */
interface LenientScorecardSlotStore {
    fun find(code: String): LenientScorecardSlot?
    fun save(item: LenientScorecardSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientScorecardSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientScorecardSlotStore(
    var items: MutableMap<String, LenientScorecardSlot>,
    var order: MutableList<String>,
) : LenientScorecardSlotStore {
    override fun find(code: String): LenientScorecardSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientScorecardSlot) {
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

    override fun list(): MutableList<LenientScorecardSlot> {
        val out = mutableListOf<LenientScorecardSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientScorecardSlotStore(): LenientScorecardSlotStore {
    return MemoryLenientScorecardSlotStore(
        mutableMapOf<String, LenientScorecardSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientScorecardSlotStore(
    var inner: LenientScorecardSlotStore,
    var cache: TtlCache<LenientScorecardSlot>,
    var hits: Int,
    var misses: Int,
) : LenientScorecardSlotStore {
    override fun find(code: String): LenientScorecardSlot? {
        val hit = (cache.get(code) as LenientScorecardSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientScorecardSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientScorecardSlot> = inner.list()
}

fun newCachedLenientScorecardSlotStore(inner: LenientScorecardSlotStore, ttl: Int, clock: Clock): LenientScorecardSlotStore {
    return CachedLenientScorecardSlotStore(
        inner = inner,
        cache = newTtlCache<LenientScorecardSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient scorecard slots. Empty fields match all. */
data class LenientScorecardSlotQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientScorecardSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientScorecardSlot>): MutableList<LenientScorecardSlot> {
        val matched = filter(items) { candidate: LenientScorecardSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientScorecardSlots(store: LenientScorecardSlotStore, query: LenientScorecardSlotQuery): MutableList<LenientScorecardSlot> {
    return query.apply(store.list())
}
