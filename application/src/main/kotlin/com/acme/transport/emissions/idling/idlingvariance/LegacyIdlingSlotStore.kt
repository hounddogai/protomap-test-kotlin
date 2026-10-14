package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy idling slots by code. */
interface LegacyIdlingSlotStore {
    fun find(code: String): LegacyIdlingSlot?
    fun save(item: LegacyIdlingSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIdlingSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIdlingSlotStore(
    var items: MutableMap<String, LegacyIdlingSlot>,
    var order: MutableList<String>,
) : LegacyIdlingSlotStore {
    override fun find(code: String): LegacyIdlingSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIdlingSlot) {
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

    override fun list(): MutableList<LegacyIdlingSlot> {
        val out = mutableListOf<LegacyIdlingSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIdlingSlotStore(): LegacyIdlingSlotStore {
    return MemoryLegacyIdlingSlotStore(mutableMapOf<String, LegacyIdlingSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIdlingSlotStore(
    var inner: LegacyIdlingSlotStore,
    var cache: TtlCache<LegacyIdlingSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyIdlingSlotStore {
    override fun find(code: String): LegacyIdlingSlot? {
        val hit = (cache.get(code) as LegacyIdlingSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIdlingSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIdlingSlot> = inner.list()
}

fun newCachedLegacyIdlingSlotStore(inner: LegacyIdlingSlotStore, ttl: Int, clock: Clock): LegacyIdlingSlotStore {
    return CachedLegacyIdlingSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyIdlingSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy idling slots. Empty fields match all. */
data class LegacyIdlingSlotQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIdlingSlot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIdlingSlot>): MutableList<LegacyIdlingSlot> {
        val matched = filter(items) { candidate: LegacyIdlingSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIdlingSlots(store: LegacyIdlingSlotStore, query: LegacyIdlingSlotQuery): MutableList<LegacyIdlingSlot> {
    return query.apply(store.list())
}
