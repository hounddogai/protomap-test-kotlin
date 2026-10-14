package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped attempt slots by code. */
interface CappedAttemptSlotStore {
    fun find(code: String): CappedAttemptSlot?
    fun save(item: CappedAttemptSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAttemptSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAttemptSlotStore(
    var items: MutableMap<String, CappedAttemptSlot>,
    var order: MutableList<String>,
) : CappedAttemptSlotStore {
    override fun find(code: String): CappedAttemptSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAttemptSlot) {
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

    override fun list(): MutableList<CappedAttemptSlot> {
        val out = mutableListOf<CappedAttemptSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAttemptSlotStore(): CappedAttemptSlotStore {
    return MemoryCappedAttemptSlotStore(mutableMapOf<String, CappedAttemptSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAttemptSlotStore(
    var inner: CappedAttemptSlotStore,
    var cache: TtlCache<CappedAttemptSlot>,
    var hits: Int,
    var misses: Int,
) : CappedAttemptSlotStore {
    override fun find(code: String): CappedAttemptSlot? {
        val hit = (cache.get(code) as CappedAttemptSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAttemptSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAttemptSlot> = inner.list()
}

fun newCachedCappedAttemptSlotStore(inner: CappedAttemptSlotStore, ttl: Int, clock: Clock): CappedAttemptSlotStore {
    return CachedCappedAttemptSlotStore(
        inner = inner,
        cache = newTtlCache<CappedAttemptSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped attempt slots. Empty fields match all. */
data class CappedAttemptSlotQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAttemptSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAttemptSlot>): MutableList<CappedAttemptSlot> {
        val matched = filter(items) { candidate: CappedAttemptSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAttemptSlots(store: CappedAttemptSlotStore, query: CappedAttemptSlotQuery): MutableList<CappedAttemptSlot> {
    return query.apply(store.list())
}
