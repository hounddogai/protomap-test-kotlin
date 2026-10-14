package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery slots by code. */
interface SeasonalRedeliverySlotStore {
    fun find(code: String): SeasonalRedeliverySlot?
    fun save(item: SeasonalRedeliverySlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliverySlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliverySlotStore(
    var items: MutableMap<String, SeasonalRedeliverySlot>,
    var order: MutableList<String>,
) : SeasonalRedeliverySlotStore {
    override fun find(code: String): SeasonalRedeliverySlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliverySlot) {
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

    override fun list(): MutableList<SeasonalRedeliverySlot> {
        val out = mutableListOf<SeasonalRedeliverySlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliverySlotStore(): SeasonalRedeliverySlotStore {
    return MemorySeasonalRedeliverySlotStore(
        mutableMapOf<String, SeasonalRedeliverySlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliverySlotStore(
    var inner: SeasonalRedeliverySlotStore,
    var cache: TtlCache<SeasonalRedeliverySlot>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliverySlotStore {
    override fun find(code: String): SeasonalRedeliverySlot? {
        val hit = (cache.get(code) as SeasonalRedeliverySlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliverySlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliverySlot> = inner.list()
}

fun newCachedSeasonalRedeliverySlotStore(inner: SeasonalRedeliverySlotStore, ttl: Int, clock: Clock): SeasonalRedeliverySlotStore {
    return CachedSeasonalRedeliverySlotStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliverySlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery slots. Empty fields match all. */
data class SeasonalRedeliverySlotQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliverySlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliverySlot>): MutableList<SeasonalRedeliverySlot> {
        val matched = filter(items) { candidate: SeasonalRedeliverySlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliverySlots(store: SeasonalRedeliverySlotStore, query: SeasonalRedeliverySlotQuery): MutableList<SeasonalRedeliverySlot> {
    return query.apply(store.list())
}
