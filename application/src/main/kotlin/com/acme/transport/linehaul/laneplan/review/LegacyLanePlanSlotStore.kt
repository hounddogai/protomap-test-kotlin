package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy lane plan slots by code. */
interface LegacyLanePlanSlotStore {
    fun find(code: String): LegacyLanePlanSlot?
    fun save(item: LegacyLanePlanSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLanePlanSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLanePlanSlotStore(
    var items: MutableMap<String, LegacyLanePlanSlot>,
    var order: MutableList<String>,
) : LegacyLanePlanSlotStore {
    override fun find(code: String): LegacyLanePlanSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLanePlanSlot) {
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

    override fun list(): MutableList<LegacyLanePlanSlot> {
        val out = mutableListOf<LegacyLanePlanSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLanePlanSlotStore(): LegacyLanePlanSlotStore {
    return MemoryLegacyLanePlanSlotStore(mutableMapOf<String, LegacyLanePlanSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLanePlanSlotStore(
    var inner: LegacyLanePlanSlotStore,
    var cache: TtlCache<LegacyLanePlanSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyLanePlanSlotStore {
    override fun find(code: String): LegacyLanePlanSlot? {
        val hit = (cache.get(code) as LegacyLanePlanSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLanePlanSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLanePlanSlot> = inner.list()
}

fun newCachedLegacyLanePlanSlotStore(inner: LegacyLanePlanSlotStore, ttl: Int, clock: Clock): LegacyLanePlanSlotStore {
    return CachedLegacyLanePlanSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyLanePlanSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lane plan slots. Empty fields match all. */
data class LegacyLanePlanSlotQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLanePlanSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LegacyLanePlanSlot>): MutableList<LegacyLanePlanSlot> {
        val matched = filter(items) { candidate: LegacyLanePlanSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLanePlanSlots(store: LegacyLanePlanSlotStore, query: LegacyLanePlanSlotQuery): MutableList<LegacyLanePlanSlot> {
    return query.apply(store.list())
}
