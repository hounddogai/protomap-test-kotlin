package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan slots by code. */
interface LanePlanSlotStore {
    fun find(code: String): LanePlanSlot?
    fun save(item: LanePlanSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanSlotStore(
    var items: MutableMap<String, LanePlanSlot>,
    var order: MutableList<String>,
) : LanePlanSlotStore {
    override fun find(code: String): LanePlanSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanSlot) {
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

    override fun list(): MutableList<LanePlanSlot> {
        val out = mutableListOf<LanePlanSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanSlotStore(): LanePlanSlotStore {
    return MemoryLanePlanSlotStore(mutableMapOf<String, LanePlanSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanSlotStore(
    var inner: LanePlanSlotStore,
    var cache: TtlCache<LanePlanSlot>,
    var hits: Int,
    var misses: Int,
) : LanePlanSlotStore {
    override fun find(code: String): LanePlanSlot? {
        val hit = (cache.get(code) as LanePlanSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanSlot> = inner.list()
}

fun newCachedLanePlanSlotStore(inner: LanePlanSlotStore, ttl: Int, clock: Clock): LanePlanSlotStore {
    return CachedLanePlanSlotStore(
        inner = inner,
        cache = newTtlCache<LanePlanSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan slots. Empty fields match all. */
data class LanePlanSlotQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanSlot>): MutableList<LanePlanSlot> {
        val matched = filter(items) { candidate: LanePlanSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanSlots(store: LanePlanSlotStore, query: LanePlanSlotQuery): MutableList<LanePlanSlot> {
    return query.apply(store.list())
}
